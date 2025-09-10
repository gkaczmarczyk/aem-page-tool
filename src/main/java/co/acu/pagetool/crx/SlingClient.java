package co.acu.pagetool.crx;

import co.acu.pagetool.OperationProperties;
import co.acu.pagetool.PageToolApp;
import co.acu.pagetool.exception.SlingClientException;
import co.acu.pagetool.util.Output;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.servlets.post.SlingPostConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A client for interacting with the Apache Sling API in Adobe Experience Manager (AEM).
 * Provides methods to query, update, copy, replace properties, and create nodes.
 *
 * @author Gregory Kaczmarczyk
 */
public class SlingClient {

    public static final String SCHEME = "http";
    public static final String SCHEME_SECURE = "https";

    private final CrxConnection conn;
    private final QueryUrl queryUrl;
    private final OperationProperties properties;
    private int lastStatusCode = -1;
    private String lastResponseText = null;

    public SlingClient(CrxConnection conn, OperationProperties properties, QueryUrl queryUrl) {
        this.conn = Objects.requireNonNull(conn, "CrxConnection must not be null");
        this.properties = Objects.requireNonNull(properties, "OperationProperties must not be null");
        this.queryUrl = Objects.requireNonNull(queryUrl, "QueryUrl must not be null");
    }

    private HttpHost createHttpHost() {
        String scheme = conn.isSecure() ? SCHEME_SECURE : SCHEME;
        return new HttpHost(conn.getHostname(), Integer.parseInt(conn.getPort()), scheme);
    }

    private CloseableHttpClient createHttpClient(HttpHost httpHost) throws SlingClientException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new org.apache.http.auth.AuthScope(httpHost.getHostName(), httpHost.getPort()),
                new org.apache.http.auth.UsernamePasswordCredentials(conn.getUsername(), conn.getPassword()));

        try {
            if (PageToolApp.bypassSSL) {
                return HttpClients.custom()
                        .setSSLHostnameVerifier((hostname, session) -> true)
                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build())
                        .setDefaultCredentialsProvider(credsProvider)
                        .build();
            }
            return HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
        } catch (Exception e) {
            throw new SlingClientException("Failed to create HTTP client", e);
        }
    }

    private HttpClientContext createClientContext(HttpHost httpHost) {
        AuthCache authCache = new BasicAuthCache();
        authCache.put(httpHost, new BasicScheme());
        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        return context;
    }

    protected String executeGet(String url) throws IOException {
        HttpHost httpHost = createHttpHost();
        try (CloseableHttpClient client = createHttpClient(httpHost)) {
            HttpClientContext context = createClientContext(httpHost);
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(httpHost, httpGet, context)) {
                lastStatusCode = response.getStatusLine().getStatusCode();
                if (lastStatusCode != 200) {
                    Output.warn("Failed to access URL: " + url + " (status code: " + lastStatusCode + ")");
                }
                return EntityUtils.toString(response.getEntity());
            }
        } catch (SlingClientException e) {
            Output.warn("Failed to initialize HTTP client: " + e.getMessage());
            throw new IOException("Unable to execute GET request due to client initialization failure", e);
        }
    }

    protected void executePost(String url, List<NameValuePair> params) throws IOException {
        HttpHost httpHost = createHttpHost();
        try (CloseableHttpClient client = createHttpClient(httpHost)) {
            HttpClientContext context = createClientContext(httpHost);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            try (CloseableHttpResponse response = client.execute(httpPost, context)) {
                lastStatusCode = response.getStatusLine().getStatusCode();
                lastResponseText = EntityUtils.toString(response.getEntity());
                if (lastStatusCode != 200 && lastStatusCode != 201) {
                    Output.warn("\nFailed to post to " + url + " (status code: " + lastStatusCode + ", response: " + lastResponseText + ")");
                } else if (PageToolApp.verbose) {
                    Output.info("Successfully posted to " + url + " (status code: " + lastStatusCode + ")");
                }
            }
        } catch (SlingClientException e) {
            Output.warn("Failed to initialize HTTP client: " + e.getMessage());
            throw new IOException("Unable to execute POST request due to client initialization failure", e);
        }
    }

    public void queryPages(String path) throws IOException {
        List<Property> propertiesList = null;
        boolean isSimplePropertyUpdate = false;
        if (properties.getDeleteProperties() != null && !properties.getDeleteProperties().isEmpty()) {
            propertiesList = properties.getDeleteProperties().stream()
                    .map(propName -> new Property(propName, ""))
                    .collect(Collectors.toList());
        } else if (properties.getPropertyValueReplacementAsList() != null) {
            propertiesList = properties.getPropertyValueReplacementAsList();
        } else if (properties.getMatchingProperties() != null && !properties.getMatchingProperties().isEmpty()) {
            propertiesList = properties.getMatchingProperties();
        } else if (properties.isPropertyCopy() && properties.getCopyFromProperties() != null && !properties.getCopyFromProperties().isEmpty()) {
            propertiesList = properties.getCopyFromProperties().stream()
                    .map(propName -> new Property(propName, ""))
                    .collect(Collectors.toList());
            if (PageToolApp.verbose) {
                Output.info("Setting properties list for property copy: " + properties.getCopyFromProperties());
            }
        } else if (properties.getUpdateProperties() != null && !properties.getUpdateProperties().isEmpty() &&
                (properties.getMatchingProperties() == null || properties.getMatchingProperties().isEmpty())) {
            isSimplePropertyUpdate = true;
            if (PageToolApp.verbose) {
                Output.info("Detected simple property update operation");
            }
        }
        String url;
        if (isSimplePropertyUpdate) {
            url = queryUrl.buildUrl(path, null, properties.getMatchingNodes(), properties.isCqPageType(), true, properties);
        } else {
            url = queryUrl.buildUrl(path, propertiesList, properties.getMatchingNodes(), properties.isCqPageType(), properties.isPropertyCopy(), properties);
        }
        if (PageToolApp.verbose) {
            Output.ninfo("Querying Sling URL: "); Output.nhl(url); Output.line();
        }
        lastResponseText = executeGet(url);
        if (PageToolApp.verbose) {
            Output.info("Status code: " + lastStatusCode);
        }
        if (PageToolApp.verbose && lastResponseText != null && !lastResponseText.isEmpty()) {
            JsonElement jsonResponse = JsonParser.parseString(lastResponseText);
            if (jsonResponse != null && jsonResponse.isJsonObject()) {
                int total = jsonResponse.getAsJsonObject().get("total").getAsInt();
                Output.ninfo("Query response (total: " + total + "): "); Output.nhl(lastResponseText); Output.line();
            } else {
                Output.ninfo("Query response: "); Output.nhl(lastResponseText); Output.line();
            }
        }
    }

    public String getPropertyValue(String path, String propertyName) throws IOException {
        String lastNode = propertyName.contains("/") ? propertyName.substring(0, propertyName.lastIndexOf("/")) : "";
        String propName = propertyName.contains("/") ? propertyName.substring(propertyName.lastIndexOf("/") + 1) : propertyName;
        if (properties.isCqPageType()) {
            path = path + (path.endsWith("/jcr:content") ? "" : "/jcr:content");
        }
        String url = queryUrl.buildUrl(path , lastNode);
        if (PageToolApp.verbose) {
            Output.info("\nFetching property value for '" + propertyName + "' at " + url);
        }
        String responseText = executeGet(url);

        if (responseText == null || responseText.isEmpty()) {
            if (PageToolApp.verbose) {
                Output.nwarn("\nEmpty response for property '" + propertyName + "' at " + url);
            }
            return null;
        }

        try {
            JsonElement root = JsonParser.parseString(responseText);
            if (!root.isJsonObject()) {
                if (PageToolApp.verbose) {
                    Output.nwarn("Invalid JSON response for property '" + propertyName + "' at " + url + ": " + responseText);
                }
                return null;
            }
            JsonElement valueElement = root.getAsJsonObject();
            if (!lastNode.isEmpty()) {
                for (String node : lastNode.split("/")) {
                    valueElement = valueElement.getAsJsonObject().get(node);
                    if (valueElement == null || !valueElement.isJsonObject()) {
                        if (PageToolApp.verbose) {
                            Output.nwarn("Node '" + node + "' not found in path for property '" + propertyName + "' at " + url);
                        }
                        return null;
                    }
                }
            }
            valueElement = valueElement.getAsJsonObject().get(propName);
            if (valueElement == null) {
                if (PageToolApp.verbose) {
                    Output.nwarn("Property '" + propName + "' not found at " + url);
                }
                return null;
            }
            if (valueElement.isJsonArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement val : valueElement.getAsJsonArray()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(val.getAsString());
                }
                return sb.toString();
            }
            return valueElement.getAsString();
        } catch (Exception e) {
            if (PageToolApp.verbose) {
                Output.nwarn("Failed to parse property '" + propertyName + "' from response at " + url + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Updates a page with properties, conditionally overwriting multi-valued properties if a match is found.
     */
    public void updatePage(String path) throws IOException {
        String url = queryUrl.buildUrl(false, path, properties.getUpdateProperties());
        List<NameValuePair> params = new ArrayList<>();

        if (properties.getDeleteProperties() != null) { // Handle deletions
            for (String prop : properties.getDeleteProperties()) {
                params.add(new BasicNameValuePair(prop + SlingPostConstants.SUFFIX_DELETE, "true"));
            }
        }

        // Handle updates with conditional overwrite for multi-valued properties
        if (properties.getUpdateProperties() != null && properties.getMatchingProperties() != null) {
            for (Property updateProp : properties.getUpdateProperties()) {
                if (updateProp.isMulti()) {
                    // Find matching property from search criteria
                    Property matchingProp = properties.getMatchingProperties().stream()
                            .filter(p -> p.getName().equals(updateProp.getName()) && p.isMulti())
                            .findFirst()
                            .orElse(null);
                    if (matchingProp != null) {
                        String targetValue = matchingProp.getValues()[0];
                        String currentValue = getPropertyValue(path, updateProp.getName());
                        if (currentValue != null && currentValue.contains(targetValue)) {
                            // Overwrite with the single value from updateProp if the search value is present
                            params.add(new BasicNameValuePair(updateProp.getName() + SlingPostConstants.TYPE_HINT_SUFFIX, "String[]"));
                            params.add(new BasicNameValuePair(updateProp.getName(), updateProp.getValues()[0]));
                        }
                    } else {
                        params.add(new BasicNameValuePair(updateProp.getName() + SlingPostConstants.TYPE_HINT_SUFFIX, "String[]"));
                        for (String value : updateProp.getValues()) {
                            params.add(new BasicNameValuePair(updateProp.getName(), value));
                        }
                    }
                } else {
                    params.add(new BasicNameValuePair(updateProp.getName(), updateProp.getValue()));
                }
            }
        } else if (properties.getUpdateProperties() != null) {
            // Fallback for non-conditional updates
            for (Property prop : properties.getUpdateProperties()) {
                if (prop.isMulti()) {
                    params.add(new BasicNameValuePair(prop.getName() + SlingPostConstants.TYPE_HINT_SUFFIX, "String[]"));
                    for (String value : prop.getValues()) {
                        params.add(new BasicNameValuePair(prop.getName(), value));
                    }
                } else {
                    params.add(new BasicNameValuePair(prop.getName(), prop.getValue()));
                }
            }
        }

        if (!params.isEmpty()) {
            executePost(url, params);
        }
    }

    public void copyProperties(String path, boolean isPropertyCopy) throws IOException {
        if (properties.getCopyFromProperties() == null || properties.getCopyToProperties() == null) {
            Output.nwarn("Copy properties are not configured.");
            return;
        }
        HttpHost httpHost = createHttpHost();
        try (CloseableHttpClient client = createHttpClient(httpHost)) {
            HttpClientContext context = createClientContext(httpHost);
            for (int i = 0; i < properties.getCopyFromProperties().size(); i++) {
                String from = properties.getCopyFromProperties().get(i);
                String to = properties.getCopyToProperties().get(i);
                if (to == null) {
                    if (PageToolApp.verbose) {
                        Output.nwarn("No target property specified for source '" + from + "'; skipping.");
                    }
                    break;
                }
                if (isPropertyCopy) {
                    String propValue = getPropertyValue(path, from);
                    if (propValue == null) {
                        if (PageToolApp.verbose) {
                            Output.nwarn("\nSource property '" + from + "' not found at " + path + "; skipping.");
                        }
                        continue;
                    }
                    String targetPath;
                    if (from.contains("/")) {
                        String parentPath = from.substring(0, from.lastIndexOf("/"));
                        String toParentPath = to.substring(0, to.lastIndexOf("/"));
                        if (!parentPath.equals(toParentPath)) {
                            if (PageToolApp.verbose) {
                                Output.nwarn("\nSource and target parent paths do not match ('" + parentPath + "' vs '" + toParentPath + "'); skipping.");
                            }
                            continue;
                        }
                        targetPath = queryUrl.buildUrl(path, parentPath);
                    } else {
                        targetPath = queryUrl.buildUrl(path, "");
                    }
                    if (!targetPath.startsWith(conn.isSecure() ? SCHEME_SECURE : SCHEME)) {
                        if (PageToolApp.verbose) {
                            Output.nwarn("\nInvalid target path for property copy: " + targetPath + "; skipping.");
                        }
                        continue;
                    }
                    String toPropName = to.contains("/") ? to.substring(to.lastIndexOf("/") + 1) : to;
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(toPropName, propValue));
                    executePost(targetPath, params);
                    if (lastStatusCode != 200 && lastStatusCode != 201) {
                        Output.nwarn("Failed to copy property from '" + from + "' to '" + to + "' at " + targetPath + " (status code: " + lastStatusCode + ")");
                        continue;
                    }
                    if (PageToolApp.verbose) {
                        Output.info("Copied property '" + from + "' to '" + to + "' at " + targetPath);
                    }
                } else {
                    String parentPath = path.substring(0, path.lastIndexOf("/"));
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(":operation", "copy"));
                    params.add(new BasicNameValuePair(":dest", parentPath + "/" + to));
                    String sourcePath = queryUrl.buildUrl(path, null, null, false, false, null, false, properties);
                    executePost(sourcePath, params);
                    if (lastStatusCode != 200 && lastStatusCode != 201) {
                        Output.nwarn("Failed to copy node from '" + from + "' to '" + to + "' at " + parentPath + "/" + to + " (status code: " + lastStatusCode + ")");
                        continue;
                    }
                    if (PageToolApp.verbose) {
                        Output.info("\nCopied node from '" + from + "' to '" + to + "' at " + parentPath + "/" + to);
                    }
                }
            }
        } catch (SlingClientException e) {
            Output.warn("Failed to initialize HTTP client: " + e.getMessage());
            throw new IOException("Unable to execute copy operation due to client initialization failure", e);
        }
    }

    public void replacePropertyValue(String path) throws SlingClientException, IOException {
        Property replacement = properties.getPropertyValueReplacement();
        if (replacement == null || replacement.getValues().length < 2) {
            throw new SlingClientException("Replacement property or values not properly configured");
        }
        String currentValue = getPropertyValue(path, replacement.getName());
        if (currentValue == null) {
            throw new SlingClientException("Unable to retrieve value for property: " + replacement.getName());
        }
        String updatedValue = currentValue.replace(replacement.getValues()[0], replacement.getValues()[1]);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(replacement.getName(), updatedValue));
        executePost(queryUrl.buildUrl(path, ""), params);
    }

    public void deleteProperties(String path) throws IOException {
        if (properties.getDeleteProperties() == null || properties.getDeleteProperties().isEmpty()) {
            Output.nwarn("No properties configured for deletion.");
            return;
        }
        String url = queryUrl.buildUrl(path, "");
        List<NameValuePair> params = new ArrayList<>();
        for (String prop : properties.getDeleteProperties()) {
            params.add(new BasicNameValuePair(prop + SlingPostConstants.SUFFIX_DELETE, "true"));
        }
        executePost(url, params);
        if (PageToolApp.verbose && lastStatusCode == 200) {
            Output.info("Successfully deleted properties for " + path + " (status code: " + lastStatusCode + ")");
        }
    }

    public void createNode(String parentPath) throws IOException {
        Property node = properties.getCreateNode();
        if (node == null) {
            Output.nwarn("No node configured for creation.");
            return;
        }
        String nodeName = node.getName();
        String primaryType = node.getValue();
        String url = queryUrl.buildUrl(parentPath, null) + "/" + nodeName;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("jcr:primaryType", primaryType));
        executePost(url, params);
        if (PageToolApp.verbose && lastStatusCode == 201) {
            Output.info("Successfully created node " + nodeName + " at " + parentPath + "/" + nodeName + " (status code: " + lastStatusCode + ")");
        }
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public String getLastResponseText() {
        return lastResponseText;
    }

}
