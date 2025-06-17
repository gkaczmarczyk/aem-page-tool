package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.SlingClient;
import co.acu.pagetool.exception.SlingClientException;
import co.acu.pagetool.util.Output;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;

/**
 * Applies operations to pages in AEM using the Sling API client.
 *
 * @author Gregory Kaczmarczyk
 */
public class PageTool {

    private final String parentNodePath;
    private final CrxConnection conn;
    private final SlingClient slingClient;

    private OperationProperties properties;
    private boolean isPropertyPath;

    public PageTool(String parentNodePath, CrxConnection conn, SlingClient slingClient) {
        this.parentNodePath = parentNodePath;
        this.conn = conn;
        this.slingClient = slingClient;
    }

    public void setProperties(OperationProperties properties) {
        this.properties = properties;
    }

    public void setIsPropertyPath(boolean isPropertyPath) {
        this.isPropertyPath = isPropertyPath;
    }

    public void executeOperation() {
        try {
            if (properties.isSearchOnly()) {
                Output.info("Performing search operation...");
                JsonArray hits = queryPages(parentNodePath);
                Output.hl("Found " + hits.size() + (properties.isCqPageType() ? " page" : " node") + (hits.size() != 1 ? "s" : "") + ".");
                for (JsonElement hit : hits) {
                    String path = hit.getAsJsonObject().get("jcr:path").getAsString();
                    Output.info(path);
                }
            } else {
                Output.info("Performing update operation...");
                processPages(parentNodePath);
            }
        } catch (IOException e) {
            Output.warn("Failed to execute operation: " + e.getMessage());
            if (PageToolApp.verbose) {
                e.printStackTrace();
            }
        }
    }

    private void processPages(String path) throws IOException {
        JsonArray pages = queryPages(path);
        Output.info("Found " + pages.size() + (properties.isCqPageType() ? " page" : " node") + (pages.size() != 1 ? "s" : "") + ".");
        if (PageToolApp.dryRun) {
            Output.hl("Dry run enabled - no updates will be performed.");
            for (JsonElement page : pages) {
                Output.info(" - " + page.getAsJsonObject().get("jcr:path").getAsString());
            }
            return;
        }
        for (JsonElement page : pages) {
            String pagePath = page.getAsJsonObject().get("jcr:path").getAsString();
            Output.ninfo("Processing " + (properties.isCqPageType() ? "page: " : "node: ") + pagePath);
            if (properties.getUpdateProperties() != null) {
                slingClient.updatePage(pagePath);
            }
            if (properties.getCopyFromProperties() != null && properties.getCopyToProperties() != null) {
                slingClient.copyProperties(pagePath);
            }
            if (properties.getPropertyValueReplacement() != null) {
                try {
                    slingClient.replacePropertyValue(pagePath);
                } catch (SlingClientException e) {
                    Output.nwarn("Failed to replace property value: " + e.getMessage());
                }
            }
            if (properties.getDeleteProperties() != null) {
                try {
                    slingClient.deleteProperties(pagePath);
                } catch (IOException e) {
                    Output.nwarn("Failed to delete properties: " + e.getMessage());
                }
            }
            if (properties.getCreateNode() != null) {
                try {
                    slingClient.createNode(pagePath);
                } catch (IOException e) {
                    Output.nwarn("Failed to create node: " + e.getMessage());
                }
            }
            Output.line();
        }
    }

    public JsonArray queryPages(String path) throws IOException {
        slingClient.queryPages(path);
        String responseText = slingClient.getLastResponseText();
        if (responseText == null || responseText.isEmpty()) {
            Output.nwarn("No response from query.");
            return new JsonArray();
        }
        JsonElement jsonResponse = JsonParser.parseString(responseText);
        if (jsonResponse == null || !jsonResponse.isJsonObject()) {
            Output.nwarn("Invalid JSON response: " + responseText);
            return new JsonArray();
        }
        JsonArray hits = jsonResponse.getAsJsonObject().getAsJsonArray("hits");
        if (hits == null) {
            Output.nwarn("No hits found in response: " + responseText);
            return new JsonArray();
        }
        return hits;
    }

}
