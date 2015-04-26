package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.SlingClient;
import co.acu.pagetool.result.ResultPage;
import co.acu.pagetool.result.ResultSet;
import co.acu.pagetool.crx.Property;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Gregory Kaczmarczyk
 */
public class PageTool {

    private CrxConnection connection;
    private String parentNodePath;
    private ArrayList<Property> matchingProperties;
    private ArrayList<Property> updateProperties;

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
        try {
            slingClient.runRead(parentNodePath, matchingProperties);
            System.out.println("");
            if (slingClient.getStatusCode() == 200) {
                Gson gson = new Gson();
                ResultSet results = gson.fromJson(slingClient.getResponseText(), ResultSet.class);
                System.out.println("Found " + results.getTotal() + " page" + (results.getTotal() == 1 ? "" : "s") + ". Updating pages now...");
                for (ResultPage page : results.getHits()) {
                    System.out.print("Updating " + page.getJcrPath() + " ...");
                    try {
                        slingClient.runUpdate(page.getJcrPath(), updateProperties);
                        if (slingClient.getStatusCode() == 200) {
                            nodesUpdated++;
                            System.out.println("OK");
                        } else {
                            System.out.println("NOT OK");
                        }
                    } catch (Exception e) {
                        System.out.println("NOT OK");
                    }
                }
                System.out.println("");
                System.out.println(nodesUpdated + " node" + (nodesUpdated == 1 ? "" : "s") + " ha" + (nodesUpdated == 1 ? "s" : "ve") + " been updated.");
            } else {
                if (slingClient.getStatusCode() < 0) {
                    System.out.println("Server appears disconnected. No changes made.");
                } else {
                    System.out.println("Error accessing URL. Received " + slingClient.getStatusCode() + " status code.");
                }
            }
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the CRX query
     */
    public void run() {
        slingClient = new SlingClient(this.connection);
//        curl.run();
        runProcess();
    }

    /**
     * Convert the properties given as options in the command line into a list of <code>Property</code> objects
     * @param propertiesStr The properties given are expected to be in the format <code>property=value</code>
     * @return ArrayList of Property objects
     */
    private ArrayList<Property> getPropertiesAsList(String[] propertiesStr) {
        ArrayList<Property> propertiesList = new ArrayList<Property>();

        for (String prop : propertiesStr) {
            String[] keyVal = prop.split("=");
            if (keyVal.length == 2) {
                propertiesList.add(new Property(keyVal[0], keyVal[1]));
            }
        }

        return propertiesList;
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

    public ArrayList<Property> getMatchingProperties() {
        return matchingProperties;
    }

    public void setMatchingProperties(ArrayList<Property> matchingProperties) {
        this.matchingProperties = matchingProperties;
    }

    public void setMatchingProperties(String[] properties) {
        this.matchingProperties = getPropertiesAsList(properties);
    }

    public ArrayList<Property> getUpdateProperties() {
        return updateProperties;
    }

    public void setUpdateProperties(ArrayList<Property> updateProperties) {
        this.updateProperties = updateProperties;
    }

    public void setUpdateProperties(String[] properties) {
        this.updateProperties = getPropertiesAsList(properties);
    }

}
