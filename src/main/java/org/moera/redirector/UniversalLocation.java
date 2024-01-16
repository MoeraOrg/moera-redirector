package org.moera.redirector;

import java.net.URI;

public class UniversalLocation {

    private final URI uri;

    private String nodeName;
    private String scheme;
    private String authority;
    private String path;

    public UniversalLocation(URI uri) {
        this.uri = uri;

        String path = uri.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return;
        }

        String[] dirs = path.split("/");
        if (!dirs[0].startsWith("@")) {
            return;
        }

        if (dirs[0].length() > 1) {
            nodeName = dirs[0].substring(1);
        }

        String host = null;
        String port = null;

        if (dirs.length > 1 && !dirs[1].equals("~")) {
            String[] parts = dirs[1].split(":");
            int i = 0;
            if (parts[i].indexOf('.') < 0) {
                scheme = parts[i++];
            }
            if (i < parts.length) {
                host = parts[i++];
            }
            if (i < parts.length) {
                port = parts[i++];
            }
        }

        if (scheme == null) {
            scheme = "https";
        }
        if (host != null && !host.isEmpty()) {
            authority = host;
            if (port != null && !port.isEmpty()) {
                authority += ':' + port;
            }
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 2; i < dirs.length; i++) {
            buf.append('/').append(dirs[i]);
        }

        this.path = buf.isEmpty() ? "/" : buf.toString();
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getScheme() {
        return scheme;
    }

    public String getAuthority() {
        return authority;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return uri.getQuery();
    }

    public String getFragment() {
        return uri.getFragment();
    }

}
