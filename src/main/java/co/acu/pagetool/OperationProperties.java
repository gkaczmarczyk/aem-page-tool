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
    private ArrayList<Property> updateProperties = null;

    private Property propertyValueReplacement = null;

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

    private Property createNode = null;

    private boolean searchOnly = false;

    private boolean cqPageType = false;

    private boolean propertyCopy = false;

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
        ArrayList<String> propStrings = new ArrayList<>();
        for (String prop : properties) {
            if (PageToolApp.verbose) {
                System.out.println("    Processing -m value: " + prop);
            }
            if (prop.contains("=")) { // Special handling for nodeName=type (e.g. jcr:content=cq:PageContent)
                int lastEqualsIndex = prop.lastIndexOf('=');
                String name = prop.substring(0, lastEqualsIndex).trim();
                String val = prop.substring(lastEqualsIndex + 1).trim();
                if ("jcr:content".equals(name)) {
                    if (matchingNodes == null) {
                        matchingNodes = new ArrayList<>();
                    }
                    matchingNodes.add(name);
                    propStrings.add("jcr:primaryType=" + val);
                    continue;
                }
            }
            propStrings.add(prop);
        }
        this.matchingProperties = getPropertiesAsList(propStrings.toArray(new String[0]));
    }

    public void setSearchValue(String[] searchValues) {
        if (searchValues == null || searchValues.length < 1) {
            return;
        }
        ArrayList<String> properties = new ArrayList<>();

        this.searchOnly = (this.updateProperties == null);
        for (String value : searchValues) {
            if (!value.contains("=")) {
                if (matchingNodes == null) {
                    matchingNodes = new ArrayList<>();
                }
                matchingNodes.add(value);
            } else {
                properties.add(value);
            }
        }

        try {
            ArrayList<Property> propList = getPropertiesAsList(properties.toArray(new String[0]));
            // Mark properties with array syntax (e.g. [value]) as multi-valued
            for (Property prop : propList) {
                if (prop.getValue() != null && !prop.getValue().isEmpty() && !prop.isMulti()) {
                    prop.setValues(new String[]{prop.getValue()});
                    prop.setMulti(true); // Flag as String[] for search
                }
            }
            setMatchingProperties(propList);
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
        if (this.searchOnly) {
            this.searchOnly = false;
        }
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
        if (this.searchOnly) {
            this.searchOnly = false;
        }
    }

    public Property getPropertyValueReplacement() {
        return propertyValueReplacement;
    }

    public ArrayList<Property> getPropertyValueReplacementAsList() {
        if (propertyValueReplacement == null) {
            return null;
        }

        ArrayList<Property> propList = new ArrayList<>();
        propList.add(propertyValueReplacement);

        return propList;
    }

    public void setPropertyValueReplacement(String[] property, String[] replacement) throws Exception {
        ArrayList<Property> properties = getPropertiesAsList(property);
        Property first = properties.get(0);
        String match = first.getValue();
        String repl = replacement[0];
        first.setValues(new String[]{match, repl});

        propertyValueReplacement = first;
    }

    private ArrayList<String> setCopyProperties(String[] properties) {
        ArrayList<String> copyPropertiesList = new ArrayList<>();

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
            this.deleteProperties = new ArrayList<>();
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

    public Property getCreateNode() {
        return createNode;
    }

    public void setCreateNode(String[] nodes) throws InvalidPropertyException {
        if (nodes == null || nodes.length == 0) {
            throw new InvalidPropertyException("Node creation requires at least one node specification");
        }
        if (PageToolApp.verbose) {
            System.out.println("  Node to create:");
        }
        Property node = Property.getProperty(nodes[0]);
        if (node == null) {
            throw new InvalidPropertyException("Invalid node format: " + nodes[0]);
        }
        if (PageToolApp.verbose) {
            System.out.println("    " + node.getName() + " = " + node.getValue());
        }
        this.createNode = node;
        this.searchOnly = false;
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
        ArrayList<Property> propertiesList = new ArrayList<>();

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

    public boolean isPropertyCopy() {
        return propertyCopy;
    }

    public void setPropertyCopy(boolean propertyCopy) {
        this.propertyCopy = propertyCopy;
        if (propertyCopy) {
            this.searchOnly = false;
        }
    }

}
