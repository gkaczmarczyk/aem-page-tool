package co.acu.pagetool.crx;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import org.apache.http.util.EntityUtils;
import org.apache.sling.servlets.post.SlingPostConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class that runs a query for AEM Pages or runs a POST to the AEM Sling servlet
 * @author Gregory Kaczmarczyk
 */
public class SlingClient {

    public static final String SCHEME = "http";

    CrxConnection conn;
    QueryUrl queryUrl;
    int statusCode = -1;
    String responseText = null;

    public SlingClient(CrxConnection conn) {
        this.conn = conn;
        this.queryUrl = new QueryUrl(conn);
    }

    /**
     * Get an instance of the HttpHost object
     * @return
     */
    private HttpHost getHttpHost() {
        return new HttpHost(conn.getHostname(), Integer.parseInt(conn.getPort()), SCHEME);
    }

    /**
     * Get an instance of the ClosableHttpClient object
     * @param httpHost
     * @return
     */
    private CloseableHttpClient getHttpClient(HttpHost httpHost) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                new UsernamePasswordCredentials(conn.getUsername(), conn.getPassword()));

        return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
    }

    /**
     * Get an instance of the HttpClientContext with authentication set
     * @param httpHost
     * @param httpClient
     * @return
     */
    private HttpClientContext getClientContext(HttpHost httpHost, CloseableHttpClient httpClient) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(httpHost, basicAuth);
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        return localContext;
    }

    /**
     * Run a query to search for all Pages under the given path which match the specified properties
     * @param path       An existing path under which AEM Pages are being searched
     * @param properties A list of properties which a page is expected to contain
     * @throws IOException
     */
    public void runRead(String path, ArrayList<Property> properties) throws IOException {
        HttpHost httpHost = getHttpHost();
        CloseableHttpClient httpClient = getHttpClient(httpHost);

        try {
            HttpClientContext clientContext = getClientContext(httpHost, httpClient);
            HttpGet httpget = new HttpGet(queryUrl.buildUrl(path, properties));

            CloseableHttpResponse response;
            try {
                response = httpClient.execute(httpHost, httpget, clientContext);
            } catch (Exception e) {
                System.out.println("There has been an error connecting to AEM. (" + e.toString() + ")");
                httpClient.close();
                return;
            }
            try {
                StatusLine status = response.getStatusLine();
                statusCode = status.getStatusCode();
                if (statusCode != 200) {
                    System.out.println("Error accessing requested URL. (status code = " + statusCode + ")");
                }
                responseText = EntityUtils.toString(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    /**
     * Get the value of the property from its given parent path
     * @param path         The node path
     * @param propertyName The name of the property. This could include subnodes within a node's jcr:content node.
     * @return The value of the given property or null if not found or if it could not be accessed fro whatever reason
     * @throws IOException
     */
    public String getPropertyValue(String path, String propertyName) throws IOException {
        String value = null;
        HttpHost httpHost = getHttpHost();
        CloseableHttpClient httpClient = getHttpClient(httpHost);

        try {
            HttpClientContext clientContext = getClientContext(httpHost, httpClient);

            String lastNode = "";
            if (propertyName.contains("/")) {
                lastNode = propertyName.substring(0, propertyName.lastIndexOf("/"));
                propertyName = propertyName.substring(propertyName.lastIndexOf("/") + 1);
            }

            HttpGet httpget = new HttpGet(queryUrl.buildUrl(path, lastNode) + ".json");

            CloseableHttpResponse response;
            try {
                response = httpClient.execute(httpHost, httpget, clientContext);
            } catch (Exception e) {
                System.out.println("There has been an error connecting to AEM. (" + e.toString() + ")");
                httpClient.close();
                return value;
            }
            try {
                StatusLine status = response.getStatusLine();
                statusCode = status.getStatusCode();
                if (statusCode != 200) {
                    System.out.println("Error accessing requested URL. (status code = " + statusCode + ")");
                }
                responseText = EntityUtils.toString(response.getEntity());
                try {
                    JsonElement root = new JsonParser().parse(responseText);
                    value = root.getAsJsonObject().get(propertyName).getAsString();
                } catch (Exception e) {
                    System.out.print(" (" + e.toString() + ") ");
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

        return value;
    }

    /**
     * Run a POST to the AEM Page at the given path updating it with the given properties
     * @param path             The page that is expected to be updated
     * @param properties       A list of properties that will be updated and/or added to the page
     * @param deleteProperties A list of properties that will be deleted from the page
     * @throws IOException
     */
    public void runUpdate(String path, ArrayList<Property> properties, ArrayList<String> deleteProperties) throws IOException {
        HttpHost httpHost = getHttpHost();
        CloseableHttpClient httpClient = getHttpClient(httpHost);

        try {
            HttpClientContext clientContext = getClientContext(httpHost, httpClient);

            HttpPost httpPost = new HttpPost(queryUrl.buildUrl(false, path, properties));
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (deleteProperties != null) {
                for (String prop : deleteProperties) {
                    nvps.add(new BasicNameValuePair(prop + SlingPostConstants.SUFFIX_DELETE, "delete-this"));
                }
            }
            if (properties != null) {
                for (Property prop : properties) {
                    if (prop.isMulti()) {
                        String[] values = prop.getValues();
                        nvps.add(new BasicNameValuePair(prop.getName() + SlingPostConstants.TYPE_HINT_SUFFIX, "String[]"));
                        for (String value : values) {
                            nvps.add(new BasicNameValuePair(prop.getName(), value));
                        }
                    } else {
                        nvps.add(new BasicNameValuePair(prop.getName(), prop.getValue()));
                    }
                }

            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response2 = httpClient.execute(httpPost, clientContext);

            try {
                this.statusCode = response2.getStatusLine().getStatusCode();
                HttpEntity entity = response2.getEntity();

                // do something useful with the response body and ensure it is fully consumed
                EntityUtils.consume(entity);
            } finally {
                response2.close();
            }
        } finally {
            httpClient.close();
        }
    }

    /**
     * Run a POST to the AEM Page  at the given path copying the values of the specified node to the specified
     * specified target node
     * @param path     The page that is expected to be updated
     * @param copyFrom A list of the properties that will have values copied
     * @param copyTo   A list of the properties that will be updated or created with new, copied values
     * @throws IOException
     */
    public void runCopy(String path, ArrayList<String> copyFrom, ArrayList<String> copyTo) throws IOException {
        HttpHost httpHost = getHttpHost();
        CloseableHttpClient httpClient = getHttpClient(httpHost);

        try {
            HttpClientContext clientContext = getClientContext(httpHost, httpClient);

            CloseableHttpResponse response = null;
            for (int i = 0; i < copyFrom.size(); i++) {
                String from = copyFrom.get(i);
                String to = copyTo.get(i);
                if (to == null) {
                    break;
                }
                HttpPost httpPost = new HttpPost(queryUrl.buildUrl(path, ""));
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair(to + SlingPostConstants.SUFFIX_COPY_FROM, from));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                response = httpClient.execute(httpPost, clientContext);
            }

            if (response != null) {
                try {
                    this.statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    // do something useful with the response body and ensure it is fully consumed
                    EntityUtils.consume(entity);
                } finally {
                    response.close();
                }
            }
        } finally {
            httpClient.close();
        }
    }

    /**
     * Get the HTTP status code of the last request performed by the SlingClient
     * @return A valid HTTP status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     *
     * @return
     */
    public String getResponseText() {
        return this.responseText;
    }

}
