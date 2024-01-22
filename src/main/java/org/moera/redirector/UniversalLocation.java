package org.moera.redirector;

import java.net.URI;

import org.moera.naming.rpc.NodeName;

public class UniversalLocation {

    private String nodeName;
    private String scheme;
    private String authority;
    private String path;
    private String query;
    private String fragment;

    public UniversalLocation(URI uri) {
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

        this.query = uri.getQuery();
        this.fragment = uri.getFragment();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = NodeName.shorten(nodeName);
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        if (scheme == null) {
            scheme = "https";
        }
        this.scheme = scheme;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setSchemeAndAuthority(URI uri) {
        setScheme(uri.getScheme());
        setAuthority(uri.getAuthority());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocation() {
        StringBuilder buf = new StringBuilder();
        buf.append("/@");
        if (nodeName != null) {
            buf.append(nodeName);
        }
        buf.append('/');
        if (authority != null) {
            if (scheme != null) {
                buf.append(scheme).append(':');
            }
            buf.append(authority);
        } else {
            buf.append('~');
        }
        buf.append(path);
        return buf.toString();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

}
