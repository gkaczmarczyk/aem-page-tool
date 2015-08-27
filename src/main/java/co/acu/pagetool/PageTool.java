package co.acu.pagetool;

import co.acu.pagetool.crx.CrxConnection;
import co.acu.pagetool.crx.SlingClient;
import co.acu.pagetool.exception.InvalidPropertyException;
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
    private ArrayList<String> copyFromProperties;
    private ArrayList<String> copyToProperties;
    private ArrayList<String> deleteProperties;

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
            System.out.println("Dry Run of update operation ...");
        }
        try {
            slingClient.runRead(parentNodePath, matchingProperties);
            System.out.println("");
            if (slingClient.getStatusCode() == 200) {
                Gson gson = new Gson();
                ResultSet results = gson.fromJson(slingClient.getResponseText(), ResultSet.class);
                System.out.print("Found " + results.getTotal() + " page" + (results.getTotal() == 1 ? "" : "s") + ". ");
                System.out.println((!PageToolApp.dryRun) ? "Updating pages now..." : "Pages to be updated:");

                for (ResultPage page : results.getHits()) {
                    System.out.print((!PageToolApp.dryRun ? "Updating " : "    ") + page.getJcrPath());
                    if (!PageToolApp.dryRun) {
                        System.out.print(" ...");
                        try {
                            slingClient.runUpdate(page.getJcrPath(), updateProperties, deleteProperties);
                            if (slingClient.getStatusCode() == 200) {
                                nodesUpdated++;
                                System.out.println("OK");
                            } else {
                                System.out.println("NOT OK" + (PageToolApp.verbose ? " (HTTP=" + slingClient.getStatusCode() + ")" : ""));
                            }
                        } catch (Exception e) {
                            System.out.println("NOT OK" + (PageToolApp.verbose ? " (" + e.toString() + ")" : ""));
                        }
                    } else {
                        System.out.println("");
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
        runProcess();
    }

    /**
     * Convert the properties given as options in the command line into a list of <code>Property</code> objects
     * @param propertiesStr The properties given are expected to be in the format <code>property=value</code>
     * @return ArrayList of Property objects
     */
    private ArrayList<Property> getPropertiesAsList(String[] propertiesStr) throws InvalidPropertyException {
        ArrayList<Property> propertiesList = new ArrayList<Property>();

        for (String propStr : propertiesStr) {
            Property property = Property.getProperty(propStr);
            if (property == null) {
                throw new InvalidPropertyException("Property is in an invalid format. (property: " + propStr + ")");
            }
            if (PageToolApp.verbose) {
                System.out.println("    " + property.getName() + " = " + property.getValue());
            }
            propertiesList.add(property);
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

    public void setMatchingProperties(String[] properties) throws InvalidPropertyException {
        if (PageToolApp.verbose) {
            System.out.println("  Properties to match:");
        }
        this.matchingProperties = getPropertiesAsList(properties);
    }

    public ArrayList<Property> getUpdateProperties() {
        return updateProperties;
    }

    public void setUpdateProperties(ArrayList<Property> updateProperties) {
        this.updateProperties = updateProperties;
    }

    public void setUpdateProperties(String[] properties) throws InvalidPropertyException {
        if (PageToolApp.verbose) {
            System.out.println("  Properties to update:");
        }
        this.updateProperties = getPropertiesAsList(properties);
    }

    private ArrayList<String> setCopyProperties(String[] properties) {
        ArrayList<String> copyPropertiesList = new ArrayList<String>();

        for (String prop : properties) {
            if (PageToolApp.verbose) {
                System.out.println("    " + prop);
            }
            copyPropertiesList.add(prop);
        }

        return copyPropertiesList;
    }

    public ArrayList<String> getCopyFromProperties() {
        return copyFromProperties;
    }

    public void setCopyFromProperties(String[] copyFromProperties) {
        if (PageToolApp.verbose) {
            System.out.println("  Properties to copy from:");
        }
        this.copyFromProperties = setCopyProperties(copyFromProperties);
    }

    public ArrayList<String> getCopyToProperties() {
        return copyToProperties;
    }

    public void setCopyToProperties(String[] copyToProperties) {
        if (PageToolApp.verbose) {
            System.out.println("  Properties to copy to:");
        }
        this.copyToProperties = setCopyProperties(copyToProperties);
    }

    public ArrayList<String> getDeleteProperties() {
        return deleteProperties;
    }

    public void setDeleteProperties(ArrayList<String> deleteProperties) {
        this.deleteProperties = deleteProperties;
    }

    public void setDeleteProperties(String[] properties) throws InvalidPropertyException {
        if (this.deleteProperties == null) {
            this.deleteProperties = new ArrayList<String>();
        }
        if (PageToolApp.verbose) {
            System.out.println("  Properties to delete:");
        }
        for (String prop : properties) {
            if (PageToolApp.verbose) {
                System.out.println("    " + prop);
            }
            this.deleteProperties.add(prop);
        }
    }

}
