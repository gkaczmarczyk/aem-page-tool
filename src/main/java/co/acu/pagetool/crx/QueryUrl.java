package co.acu.pagetool.crx;

import java.util.ArrayList;

public class QueryUrl {

    static final String QUERY_PATH = "/bin/querybuilder.json";

    /**
     * The number of maximum results to display per request
     */
    int limit = 1000;

    /**
     * The hierarchical depth to search for nodes/Pages
     */
    int nodeDepth = 5;

    /**
     * The type of output from each node
     */
    String hitsType = "selective";

    /**
     * The node properties to output in the JSON response
     */
    String hitsProperties = "jcr:path";

    CrxConnection conn;
    Property nodeType;
    boolean isCqPageType = true;

    public QueryUrl(CrxConnection conn) {
        this.conn = conn;
        nodeType = new Property("type", "cq:Page");
    }

    /**
     * Get the Sling URL which includes the scheme & hostname
     * @return
     */
    private StringBuilder getHostUrl() {
        StringBuilder sb = new StringBuilder();

        sb.append(SlingClient.SCHEME)
                .append("://")
                .append(conn.getHostname())
                .append(':')
                .append(conn.getPort());

        return sb;
    }

    /**
     * Builds all of the extra parameters that are passed to the Sling query
     * @param path
     * @param properties
     * @param nodes
     * @return
     */
    private StringBuilder getQueryPath(String path, ArrayList<Property> properties, ArrayList<String> nodes) {
        StringBuilder sb = new StringBuilder();

        sb.append(QUERY_PATH)
                .append('?')
                .append("path")
                .append('=')
                .append(path)
                .append("&p.limit=")
                .append(limit)
                .append("&p.hits=")
                .append(hitsType)
                .append("&p.properties=")
                .append(hitsProperties)
                .append("&p.nodedepth=")
                .append(nodeDepth);

        if (isCqPageType) {
            sb.append('&')
                    .append(nodeType.getName())
                    .append('=')
                    .append(nodeType.getValue());
        }

        if (properties != null) {
            int propCtr = (properties.size() > 1) ? 1 : -1;
            for (Property prop : properties) {
                String propKey = ((propCtr < 1) ? "" : propCtr++ + "_") + "property";
                sb.append('&')
                        .append(propKey)
                        .append('=');

                if (isCqPageType) {
                    sb.append("jcr:content/");
                }

                sb.append(prop.getName())
                        .append('&')
                        .append(propKey)
                        .append(".value=")
                        .append(prop.getValue());
            }
        }

        if (nodes != null) {
            sb.append("&nodename=")
                    .append(nodes.get(0));
        }

        return sb;
    }

    /**
     * Build the Sling URL
     * @param path The top level JCR node path which should be searched
     * @param properties
     * @param nodes A list of node names that are being searched
     * @return
     */
    public String buildUrl(String path, ArrayList<Property> properties, ArrayList<String> nodes) {
        return buildUrl(true, path, properties, nodes);
    }

    /**
     * Build the Sling URL
     * @param path The top level JCR node path which should be searched
     * @param properties
     * @param nodes A list of node names that are being searched
     * @param cqPageType Whether or not the node type is a cq:Page
     * @return
     */
    public String buildUrl(String path, ArrayList<Property> properties, ArrayList<String> nodes, boolean cqPageType) {
        this.isCqPageType = cqPageType;

        return buildUrl(true, path, properties, nodes);
    }

    /**
     * Build the Sling URL
     * @param isQuery Whether or not the this Sling request is a query
     * @param path The top level JCR node path which should be searched
     * @param properties
     * @return
     */
    public String buildUrl(boolean isQuery, String path, ArrayList<Property> properties) {
        return buildUrl(isQuery, path, properties, null, null);
    }

    /**
     * Build the Sling URL
     * @param isQuery Whether or not the this Sling request is a query
     * @param path The top level JCR node path which should be searched
     * @param properties
     * @param nodes A list of node names that are being searched
     * @return
     */
    public String buildUrl(boolean isQuery, String path, ArrayList<Property> properties, ArrayList<String> nodes) {
        return buildUrl(isQuery, path, properties, nodes, null);
    }

    /**
     * Build the Sling URL
     * @param path The top level JCR node path which should be searched
     * @param copyProperty
     * @return
     */
    public String buildUrl(String path, String copyProperty) {
        return buildUrl(false, path, null, null, copyProperty);
    }

    /**
     * Build the Sling URL
     * @param isQuery Whether or not the this Sling request is a query
     * @param path The top level JCR node path which should be searched
     * @param properties
     * @param nodes A list of node names that are being searched
     * @param copyProperty The name of the property that is being copied
     * @return
     */
    public String buildUrl(boolean isQuery, String path, ArrayList<Property> properties, ArrayList<String> nodes, String copyProperty) {
        StringBuilder sb = getHostUrl();

        if (isQuery) {
            sb.append(getQueryPath(path, properties, nodes));
        } else {
            sb.append(path);

            if (this.isCqPageType) {
                sb.append("/jcr:content");
            }

            if (copyProperty != null && !copyProperty.equals("")) {
                sb.append("/").append(copyProperty);
            }
        }

        return sb.toString();
    }

}
