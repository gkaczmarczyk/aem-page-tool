package co.acu.pagetool.crx;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class that runs a query for AEM Pages or runs a POST to the AEM Sling servlet
 * @author Gregory Kaczmarczyk
 */
public class SlingClient {

    static final String SCHEME = "http";
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
    int statusCode = -1;
    String responseText = null;

    public SlingClient(CrxConnection conn) {
        this.conn = conn;
        nodeType = new Property("type", "cq:Page");
    }

    private String buildUrl(String path, ArrayList<Property> properties) {
        return buildUrl(true, path, properties);
    }

    private String buildUrl(boolean isQuery, String path, ArrayList<Property> properties) {
        StringBuilder sb = new StringBuilder();

        sb.append(SCHEME)
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

            char appendPropChar = '?';
            if (properties != null) {
                for (Property prop : properties) {
                    sb.append(appendPropChar);
                    if (appendPropChar == '?') {
                        appendPropChar = '&';
                    }
                    sb.append("jcr:content/")
                            .append(prop.getName())
                            .append('=')
                            .append(prop.getValue());
                }
            }
        }

        return sb.toString();
    }

    /**
     * Run a query to search for all Pages under the given path which match the specified properties
     * @param path       An existing path under which AEM Pages are being searched
     * @param properties A list of properties which a page is expected to contain
     * @throws IOException
     */
    public void runRead(String path, ArrayList<Property> properties) throws IOException {
        HttpHost target = new HttpHost(conn.getHostname(), Integer.parseInt(conn.getPort()), SCHEME);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(conn.getUsername(), conn.getPassword()));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        try {
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpGet httpget = new HttpGet(buildUrl(path, properties));

//            System.out.println("Executing request " + httpget.getRequestLine() + " to target " + target);
            CloseableHttpResponse response;
            try {
                response = httpclient.execute(target, httpget, localContext);
            } catch (Exception e) {
                System.out.println("There has been an error connecting to AEM. (" + e.toString() + ")");
                httpclient.close();
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
            httpclient.close();
        }
    }

    /**
     * Run a POST to the AEM Page at the given path updating it with the given properties
     * @param path       The page that is expected to be updated
     * @param properties A list of properties that will be updated and/or added to the page
     * @throws IOException
     */
    public void runUpdate(String path, ArrayList<Property> properties) throws IOException {
        HttpHost target = new HttpHost(conn.getHostname(), Integer.parseInt(conn.getPort()), SCHEME);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(conn.getUsername(), conn.getPassword()));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        try {
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpPost httpPost = new HttpPost(buildUrl(false, path, properties));
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("_generic_prop", "new degree type"));
            for (Property prop : properties) {
                nvps.add(new BasicNameValuePair(prop.getName(), prop.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response2 = httpclient.execute(httpPost, localContext);

            try {
                this.statusCode = response2.getStatusLine().getStatusCode();
                HttpEntity entity2 = response2.getEntity();

                // do something useful with the response body and ensure it is fully consumed
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } finally {
            httpclient.close();
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
