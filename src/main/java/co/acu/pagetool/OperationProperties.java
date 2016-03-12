package co.acu.pagetool;

import co.acu.pagetool.crx.Property;
import co.acu.pagetool.exception.InvalidPropertyException;

import java.util.ArrayList;

/**
 * A container for all the properties of operations that should be performed with the Sling client
 * @author Greg Kaczmarczyk
 */
public class OperationProperties {

    /**
     * A list of properties which a page is expected to contain
     */
    private ArrayList<Property> matchingProperties;

    /**
     * A list of nodes that is expected to be found.
     */
    private ArrayList<String> matchingNodes = null;

    /**
     * A list of properties that will be updated and/or added to the page
     */
    private ArrayList<Property> updateProperties;

    /**
     * A list of the properties that will have values copied
     */
    private ArrayList<String> copyFromProperties;

    /**
     * A list of the properties that will be updated or created with new, copied values
     */
    private ArrayList<String> copyToProperties;

    /**
     * A list of properties that will be deleted from the page
     */
    private ArrayList<String> deleteProperties;

    private boolean searchOnly = false;

    private boolean cqPageType = true;

    /**
     * Get the set list of matching properties
     * @return A list of properties which a page is expected to contain
     */
    public ArrayList<Property> getMatchingProperties() {
        return matchingProperties;
    }

    /**
     * Set the set list of matching properties
     * @param matchingProperties A list of properties which a page is expected to contain
     */
    public void setMatchingProperties(ArrayList<Property> matchingProperties) {
        this.matchingProperties = matchingProperties;
    }

    /**
     * Set the set list of matching properties
     * @param properties An array of a list of properties which a page is expected to contain
     * @throws InvalidPropertyException
     */
    public void setMatchingProperties(String[] properties) throws InvalidPropertyException {
        if (PageToolApp.verbose) {
            System.out.println("  Properties to match:");
        }
        this.matchingProperties = getPropertiesAsList(properties);
    }

    public void setSearchValue(String[] searchValues) {
        if (searchValues == null || searchValues.length < 1) {
            return;
        }
        ArrayList<String> properties = new ArrayList<String>();

        this.searchOnly = true;
        for (String value : searchValues) {
            if (!value.contains("=")) {
                if (matchingNodes == null) {
                    matchingNodes = new ArrayList<String>();
                }
                matchingNodes.add(value);
            } else {
                properties.add(value);
            }
        }

        try {
            setMatchingProperties(properties.toArray(new String[properties.size()]));
        } catch (InvalidPropertyException e) {
            System.out.println("Error setting the search property (" + e.toString() + ").");
        }
    }

    public ArrayList<String> getMatchingNodes() {
        return this.matchingNodes;
    }

    /**
     * Get a list of update properties
     * @return A list of properties that will be updated and/or added to the page
     */
    public ArrayList<Property> getUpdateProperties() {
        return updateProperties;
    }

    /**
     * Set a list of update properties
     * @param updateProperties A list of properties that will be updated and/or added to the page
     */
    public void setUpdateProperties(ArrayList<Property> updateProperties) {
        this.updateProperties = updateProperties;
    }

    /**
     * Set a list of update properties
     * @param properties An array of a list of properties that will be updated and/or added to the page
     * @throws InvalidPropertyException
     */
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

    /**
     * Get whether the operation is a search only.
     * @return If true, the operation is not expected to perform any changes to the JCR
     */
    public boolean isSearchOnly() {
        return searchOnly;
    }

    public boolean isCqPageType() {
        return cqPageType;
    }

    public void setCqPageType(boolean cqPageType) {
        this.cqPageType = cqPageType;
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

}
