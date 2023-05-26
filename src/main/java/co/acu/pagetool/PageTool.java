package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.SlingClient;
import co.acu.pagetool.result.ResultPage;
import co.acu.pagetool.result.ResultSet;
import co.acu.pagetool.crx.Property;
import co.acu.pagetool.util.Output;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import static co.acu.pagetool.util.Output.NOT_OK;
import static co.acu.pagetool.util.Output.OK;

/**
 * @author Gregory Kaczmarczyk
 */
public class PageTool {

    private CrxConnection connection;
    private String parentNodePath;
    private boolean isPropertyPath;

    private OperationProperties properties;
    private SlingClient slingClient;
    private int nodesUpdated = 0;

    /**
     * Class constructor
     * @param parentNodePath The path under which all searches should be run
     */
    public PageTool(String parentNodePath) {
        this.parentNodePath = parentNodePath;

        connection = new CrxConnection();
    }

    private void runProcess() {
        if (PageToolApp.dryRun) {
            Output.info("Dry Run of update operation ...");
        }
        try {
            slingClient.runRead(parentNodePath);
            Output.line();
            if (slingClient.getStatusCode() == 200) {
                Gson gson = new Gson();
                ResultSet results = gson.fromJson(slingClient.getResponseText(), ResultSet.class);
                if (!properties.isSearchOnly()) {
                    Output.nhl("Found " + results.getTotal() + (properties.isCqPageType() ? " page" : " node") + (results.getTotal() == 1 ? "" : "s") + ". ");
                    if (results.getTotal() > 0) {
                        Output.hl((!PageToolApp.dryRun) ? "Updating pages now..." : "Pages to be updated:");
                    } else {
                        Output.line();
                    }
                }

                for (ResultPage page : results.getHits()) {
                    if (properties.isSearchOnly()) {
                        Output.info("    " + page.getJcrPath());
                        continue;
                    }
                    Output.ninfo((!PageToolApp.dryRun ? "Updating " : "    ") + page.getJcrPath());
                    if (!PageToolApp.dryRun) {
                        Output.ninfo(" ...");
                        try {
                            // we're making copying properties take precedence to updates or deletes
                            if (properties.getCopyFromProperties() != null && properties.getCopyFromProperties().size() > 0 && properties.getCopyFromProperties().size() == properties.getCopyToProperties().size()) {
                                if (isPropertyPath) {
                                    String fromPropertyValue = slingClient.getPropertyValue(page.getJcrPath(), properties.getCopyFromProperties().get(0));
                                    if (fromPropertyValue == null) {
                                        Output.info(NOT_OK);
                                        continue;
                                    }
                                    if (properties.getUpdateProperties().size() > 1) { // just make sure there's only one copy target
                                        Property newProperty = new Property(properties.getCopyToProperties().get(0), fromPropertyValue);
                                        ArrayList<Property> copyToList = new ArrayList<>();
                                        copyToList.add(newProperty);
                                        properties.setUpdateProperties(copyToList);
                                    }
                                    slingClient.runUpdate(page.getJcrPath());
                                } else {
                                    slingClient.runCopy(page.getJcrPath());
                                }
                            } else {
                                slingClient.runUpdate(page.getJcrPath());
                            }
                            if (slingClient.getStatusCode() == 200) {
                                nodesUpdated++;
                                Output.info(OK);
                            } else {
                                Output.info(NOT_OK + (PageToolApp.verbose ? " (HTTP=" + slingClient.getStatusCode() + ")" : ""));
                            }
                        } catch (Exception e) {
                            Output.info(NOT_OK + (PageToolApp.verbose ? " (" + e.toString() + ")" : ""));
                        }
                    } else {
                        Output.line();
                    }
                }

                if (properties.isSearchOnly()) {
                    Output.nhl("\n  " + results.getTotal() + (properties.isCqPageType() ? " page" : " node") +
                            (results.getTotal() == 1 ? " was" : "s were") + " found. ");
                } else {
                    if (results.getTotal() > 0) {
                        Output.line();
                        Output.hl(nodesUpdated + " node" + (nodesUpdated == 1 ? "" : "s") + " ha" + (nodesUpdated == 1 ? "s" : "ve") + " been updated.");
                    }
                }
                Output.line();
            } else {
                if (slingClient.getStatusCode() < 0) {
                    Output.info("Server appears disconnected. No changes made.");
                } else {
                    Output.warn("Error accessing URL. Received " + slingClient.getStatusCode() + " status code.");
                }
            }
            Output.line();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the CRX query
     */
    public void run() {
        slingClient = new SlingClient(this.connection, properties);
        runProcess();
    }

    public String getParentNodePath() {
        return parentNodePath;
    }

    public void setParentNodePath(String parentNodePath) {
        this.parentNodePath = parentNodePath;
    }

    public CrxConnection getConnection() {
        return connection;
    }

    public void setConnection(CrxConnection connection) {
        this.connection = connection;
    }

    public void setProperties(OperationProperties properties) {
        this.properties = properties;
    }

    public boolean isPropertyPath() {
        return isPropertyPath;
    }

    public void setIsPropertyPath(boolean isPropertyPath) {
        this.isPropertyPath = isPropertyPath;
    }

}
