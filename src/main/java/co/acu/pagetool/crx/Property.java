package co.acu.pagetool.crx;

import co.acu.pagetool.exception.InvalidPropertyException;

/**
 * Simple representation of a node property
 * @author Greg Kaczmarczyk
 */
public class Property {

    private String name;
    private String value;
    private String[] values;
    private boolean isMulti = false;

    public static Property getProperty(String keyVal) throws InvalidPropertyException {
        if (keyVal == null || keyVal.trim().isEmpty()) {
            return null;
        }
        int lastEqualsIndex = keyVal.lastIndexOf('=');
        if (lastEqualsIndex == -1) {
            throw new InvalidPropertyException("Property must contain '=': " + keyVal);
        }
        String name = keyVal.substring(0, lastEqualsIndex).trim();
        String value = keyVal.substring(lastEqualsIndex + 1).trim();
        if (name.isEmpty()) {
            throw new InvalidPropertyException("Property name cannot be empty: " + keyVal);
        }
        if (value.startsWith("[") && value.endsWith("]")) { // Handle array syntax: [value1,value2]
            String[] values = value.substring(1, value.length() - 1).split(",");
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }
            return new Property(name, values);
        }
        return new Property(name, value);
    }

    public Property() {
    }

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Property(String name, String[] values) {
        this.name = name;
        this.values = values;
        this.isMulti = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
        this.isMulti = true;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public void setMulti(boolean isMulti) {
        this.isMulti = isMulti;
    }

}
