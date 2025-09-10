package co.acu.pagetool.crx;

import co.acu.pagetool.OperationProperties;
import co.acu.pagetool.PageToolApp;
import co.acu.pagetool.util.Output;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for constructing URLs for Sling API queries and updates in AEM.
 *
 * @author Gregory Kaczmarczyk
 */
public class QueryUrl {

    private static final String QUERY_PATH = "/bin/querybuilder.json";
    private static final int DEFAULT_LIMIT = 1000;
    private static final int DEFAULT_NODE_DEPTH = 10;
    private static final String DEFAULT_HITS_TYPE = "selective";
    private static final String DEFAULT_HITS_PROPERTIES = "jcr:path";

    private final CrxConnection conn;
    private final int limit;
    private final int nodeDepth;
    private final String hitsType;
    private final String hitsProperties;
    private boolean isCqPageType;

    public QueryUrl(CrxConnection conn) {
        this.conn = Objects.requireNonNull(conn, "CrxConnection must not be null");
        this.limit = DEFAULT_LIMIT;
        this.nodeDepth = DEFAULT_NODE_DEPTH;
        this.hitsType = DEFAULT_HITS_TYPE;
        this.hitsProperties = DEFAULT_HITS_PROPERTIES;
        this.isCqPageType = false;
    }

    private StringBuilder buildBaseUrl() {
        return new StringBuilder()
                .append(conn.isSecure() ? SlingClient.SCHEME_SECURE : SlingClient.SCHEME)
                .append("://")
                .append(conn.getHostname())
                .append(':')
                .append(conn.getPort());
    }

    private StringBuilder buildQueryParameters(String path, List<Property> properties, List<String> nodes, boolean isPropertyCopy, OperationProperties opProps) {
        StringBuilder sb = new StringBuilder()
                .append(QUERY_PATH)
                .append("?path=").append(path)
                .append("&p.limit=").append(limit)
                .append("&p.hits=").append(hitsType)
                .append("&p.properties=").append(hitsProperties)
                .append("&p.nodedepth=").append(nodeDepth);

        boolean hasPropertyCopyFilter = false;
        boolean isPropertyOperation = false;

        // property copy filtering
        if (opProps != null && opProps.getCopyFromProperties() != null && !opProps.getCopyFromProperties().isEmpty()) {
            String propName = opProps.getCopyFromProperties().get(0);
            if (isPropertyCopy) {
                sb.append("&property=").append(propName)
                        .append("&property.operation=exists");
                hasPropertyCopyFilter = true;
                isPropertyOperation = true;

                if (PageToolApp.verbose) {
                    Output.info("Filtering query for property: " + propName);
                }
            } else {
                sb.append("&nodename=").append(propName);
                hasPropertyCopyFilter = true;
                if (PageToolApp.verbose) {
                    Output.info("Filtering query for node: " + propName);
                }
            }
        }

        // match properties for search/update operations
        if (properties != null && !properties.isEmpty() && !hasPropertyCopyFilter) {
            int propCtr = 1;
            for (Property prop : properties) {
                String propKey = propCtr++ + "_property";
                String searchProperty = prop.getName();
                sb.append('&').append(propKey).append('=').append(searchProperty);
                isPropertyOperation = true; // This is a property operation

                if (!prop.isMulti()) {
                    if (prop.getValue() != null && !prop.getValue().isEmpty()) {
                        sb.append('&').append(propKey).append(".value=").append(URLEncoder.encode(prop.getValue(), StandardCharsets.UTF_8));
                    } else {
                        sb.append('&').append(propKey).append(".operation=exists");
                    }
                } else if (prop.isMulti() && prop.getValues() != null && prop.getValues().length > 0) {
                    String likeValue = "%" + prop.getValues()[0] + "%";
                    sb.append('&').append(propKey).append(".operation=like")
                            .append('&').append(propKey).append(".value=").append(URLEncoder.encode(likeValue, StandardCharsets.UTF_8));
                    if (PageToolApp.verbose) {
                        Output.info("Searching " + searchProperty + " for value: " + prop.getValues()[0]);
                    }
                }
            }
        }

        // simple property updates without search criteria
        if (isPropertyCopy && (properties == null || properties.isEmpty()) && !hasPropertyCopyFilter) {
            isPropertyOperation = true;
            if (PageToolApp.verbose) {
                Output.info("Detected simple property update operation without search criteria");
            }
        }

        if (nodes != null && !nodes.isEmpty()) {
            sb.append("&nodename=").append(nodes.get(0));
        }

        if (isCqPageType) {
            if (isPropertyOperation) {
                sb.append("&type=cq:PageContent");
            } else {
                sb.append("&type=cq:Page");
            }
        }

        return sb;
    }

    public String buildUrl(String path, List<Property> properties, List<String> nodes, boolean isQuery, boolean cqPageType, String copyProperty, boolean isPropertyCopy, OperationProperties opProps) {
        this.isCqPageType = cqPageType;
        StringBuilder sb = buildBaseUrl();
        if (isQuery) {
            sb.append(buildQueryParameters(path, properties, nodes, isPropertyCopy, opProps));
        } else {
            sb.append(path);
            if (cqPageType && !path.endsWith("/jcr:content")) {
                sb.append("/jcr:content");
            }
            if (copyProperty != null && !copyProperty.isEmpty()) {
                sb.append("/").append(copyProperty);
            }
            sb.append(".json");
        }

        return sb.toString();
    }

    public String buildUrl(String path, List<Property> properties, List<String> nodes, boolean cqPageType, boolean isPropertyOperation, OperationProperties opProps) {
        return buildUrl(path, properties, nodes, true, cqPageType, null, isPropertyOperation, opProps);
    }

    public String buildUrl(boolean isQuery, String path, List<Property> properties) {
        return buildUrl(path, properties, null, isQuery, isCqPageType, null, false, null);
    }

    public String buildUrl(String path, String copyProperty) {
        return buildUrl(path, null, null, false, isCqPageType, copyProperty, false, null);
    }

}
