package org.moera.redirector;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.moera.naming.rpc.NamingService;
import org.moera.naming.rpc.RegisteredName;
import org.moera.naming.rpc.RegisteredNameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingCache {

    private static final Logger log = LoggerFactory.getLogger(NamingCache.class);

    private static final String NAMING_SERVICE_URL = "https://naming.moera.org/moera-naming";
    private static final int NAMING_THREADS = 16;
    private static final Duration NORMAL_TTL = Duration.of(6, ChronoUnit.HOURS);
    private static final Duration ERROR_TTL = Duration.of(1, ChronoUnit.MINUTES);

    private final NamingService namingService;
    private final ExecutorService executor = Executors.newFixedThreadPool(NAMING_THREADS);

    private final class Record {

        private final String nodeName;
        private Instant accessed = Instant.now();
        private Instant deadline;
        private NodeUrl url;
        private List<CompletableFuture<NodeUrl>> waitList;
        private boolean fetching;
        private final Object lock = new Object();

        public Record(String nodeName) {
            this.nodeName = nodeName;
            waitList = new ArrayList<>();
        }

        public boolean isLoaded() {
            return waitList == null;
        }

        public void reload() {
            synchronized (lock) {
                if (isLoaded()) {
                    deadline = null;
                    waitList = new ArrayList<>();
                    fetch();
                }
            }
        }

        public Future<NodeUrl> getUrl() {
            accessed = Instant.now();
            if (isLoaded()) {
                return CompletableFuture.completedFuture(url);
            }
            synchronized (lock) {
                if (isLoaded()) {
                    return CompletableFuture.completedFuture(url);
                }
                CompletableFuture<NodeUrl> future = new CompletableFuture<>();
                waitList.add(future);
                fetch();
                return future;
            }
        }

        private void setUrl(NodeUrl url) {
            List<CompletableFuture<NodeUrl>> list;
            synchronized (lock) {
                if (isLoaded()) {
                    throw new IllegalStateException("URL is loaded already");
                }
                this.url = url;
                deadline = Instant.now().plus(url != null ? NORMAL_TTL : ERROR_TTL);

                list = waitList;
                waitList = null;
            }
            list.forEach(future -> future.complete(url));
        }

        private void fetch() {
            if (!fetching) {
                fetching = true;
                executor.submit(this::fetchName);
            }
        }

        private void fetchName() {
            try {
                RegisteredName registeredName = RegisteredName.parse(nodeName);
                RegisteredNameInfo info =
                        namingService.getCurrent(registeredName.getName(), registeredName.getGeneration());
                setUrl(new NodeUrl(info != null ? info.getNodeUri() : null));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                setUrl(null);
            }
        }

        public boolean isExpired() {
            return deadline != null && deadline.isBefore(Instant.now());
        }

        public boolean isPopular() {
            return accessed.plus(NORMAL_TTL).isAfter(Instant.now());
        }

    }

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final Map<String, Record> cache = new HashMap<>();

    public NamingCache() throws MalformedURLException {
        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(NAMING_SERVICE_URL));
        namingService = ProxyUtil.createClientProxy(getClass().getClassLoader(), NamingService.class, client);

        Thread purgeThread = new Thread(this::purgeRunner);
        purgeThread.setDaemon(true);
        purgeThread.start();
    }

    public Optional<NodeUrl> getFast(String nodeName) {
        Future<NodeUrl> future = getOrRun(nodeName);
        try {
            return future.isDone() ? Optional.of(future.get()) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }

    public NodeUrl get(String nodeName) {
        try {
            return getOrRun(nodeName).get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private Future<NodeUrl> getOrRun(String nodeName) {
        nodeName = RegisteredName.expand(nodeName);
        Record record;
        cacheLock.readLock().lock();
        try {
            record = cache.get(nodeName);
        } finally {
            cacheLock.readLock().unlock();
        }
        if (record == null) {
            cacheLock.writeLock().lock();
            try {
                record = cache.get(nodeName);
                if (record == null) {
                    record = new Record(nodeName);
                    cache.put(nodeName, record);
                }
            } finally {
                cacheLock.writeLock().unlock();
            }
        }
        return record.getUrl();
    }

    private void purgeRunner() {
        try {
            while (true) {
                Thread.sleep(60000);
                purge();
            }
        } catch (InterruptedException e) {
            // interrupted
        }
    }

    private void purge() {
        List<String> remove;
        cacheLock.readLock().lock();
        try {
            remove = cache.entrySet().stream()
                    .filter(e -> e.getValue().isExpired())
                    .map(Map.Entry::getKey)
                    .toList();
        } finally {
            cacheLock.readLock().unlock();
        }
        if (!remove.isEmpty()) {
            cacheLock.writeLock().lock();
            try {
                remove.forEach(key -> {
                    Record record = cache.get(key);
                    if (record.isPopular()) {
                        record.reload();
                    } else {
                        cache.remove(key);
                    }
                });
            } finally {
                cacheLock.writeLock().unlock();
            }
        }
    }

}
