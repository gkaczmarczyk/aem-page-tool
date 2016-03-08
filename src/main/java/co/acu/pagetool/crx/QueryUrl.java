package co.acu.pagetool.crx;

import java.util.ArrayList;

public class QueryUrl {

//    static final String SCHEME = "http";
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

    public QueryUrl(CrxConnection conn) {
        this.conn = conn;
        nodeType = new Property("type", "cq:Page");
    }

    public String buildUrl(String path, ArrayList<Property> properties) {
        return buildUrl(true, path, properties, null);
    }

    public String buildUrl(boolean isQuery, String path, ArrayList<Property> properties) {
        return buildUrl(isQuery, path, properties, null);
    }

    public String buildUrl(String path, String copyProperty) {
        return buildUrl(false, path, null, copyProperty);
    }

    public String buildUrl(boolean isQuery, String path, ArrayList<Property> properties, String copyProperty) {
        StringBuilder sb = new StringBuilder();

        sb.append(SlingClient.SCHEME)
                .append("://")
                .append(conn.getHostname())
                .append(':')
                .append(conn.getPort());

        if (isQuery) {
            sb.append(QUERY_PATH)
                    .append('?')
                    .append(nodeType.getName())
                    .append('=')
                    .append(nodeType.getValue())
                    .append('&')
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

            if (properties != null) {
                int propCtr = (properties.size() > 1) ? 1 : -1;
                for (Property prop : properties) {
                    String propKey = ((propCtr < 1) ? "" : propCtr++ + "_") + "property";
                    sb.append('&')
                            .append(propKey)
                            .append('=')
                            .append("jcr:content/")
                            .append(prop.getName())
                            .append('&')
                            .append(propKey)
                            .append(".value=")
                            .append(prop.getValue());
                }
            }
        } else {
            sb.append(path).append("/jcr:content");

            if (copyProperty != null && !copyProperty.equals("")) {
                sb.append("/").append(copyProperty);
            }
        }

        return sb.toString();
    }

}
