package co.acu.pagetool.crx;

/**
 * Simple representation of a node property
 * @author Greg Kaczmarczyk
 */
public class Property {

    private String name;
    private String value;
    private String[] values;
    private boolean isMulti = false;

    public static Property getProperty(String keyVal) {
        Property property = null;
        String[] keyValArr = keyVal.split("=");
        if (keyValArr.length == 2) {
            if (keyValArr[1].matches("^\\[.*\\]$")) {
                property = new Property(keyValArr[0], keyValArr[1].replaceAll("^\\[", "").replaceAll("\\]$", "").split(","));
            } else {
                property = new Property(keyValArr[0], keyValArr[1]);
            }
        }

        return property;
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
