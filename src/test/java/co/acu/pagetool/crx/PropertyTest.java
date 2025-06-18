package co.acu.pagetool.crx;

import co.acu.pagetool.exception.InvalidPropertyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PropertyTest {

    private Property property;

    @BeforeEach
    void setUp() {
        property = new Property();
    }

    @Test
    void testGetProperty_ValidSingleValue() throws InvalidPropertyException {
        Property prop = Property.getProperty("key=value");
        assertEquals("key", prop.getName(), "Name should be 'key'");
        assertEquals("value", prop.getValue(), "Value should be 'value'");
        assertNull(prop.getValues(), "Values should be null for single value");
        assertFalse(prop.isMulti(), "Should not be multi-valued");
    }

    @Test
    void testGetProperty_ValidMultiValue() throws InvalidPropertyException {
        Property prop = Property.getProperty("key=[value1,value2]");
        assertEquals("key", prop.getName(), "Name should be 'key'");
        assertNull(prop.getValue(), "Value should be null for multi-value");
        assertTrue(Arrays.equals(new String[]{"value1", "value2"}, prop.getValues()), "Values should match");
        assertTrue(prop.isMulti(), "Should be multi-valued");
    }

    @Test
    void testGetProperty_TrimmedWhitespace() throws InvalidPropertyException {
        Property prop = Property.getProperty("key = value");
        assertEquals("key", prop.getName(), "Name should be trimmed to 'key'");
        assertEquals("value", prop.getValue(), "Value should be trimmed to 'value'");
        assertNull(prop.getValues(), "Values should be null");
        assertFalse(prop.isMulti(), "Should not be multi-valued");
    }

    @Test
    void testGetProperty_NullInput() throws InvalidPropertyException {
        Property prop = Property.getProperty(null);
        assertNull(prop, "Should return null for null input");
    }

    @Test
    void testGetProperty_EmptyInput() throws InvalidPropertyException {
        Property prop = Property.getProperty("");
        assertNull(prop, "Should return null for empty input");
        prop = Property.getProperty(" ");
        assertNull(prop, "Should return null for blank input");
    }

    @Test
    void testGetProperty_NoEquals() {
        InvalidPropertyException exception = assertThrows(InvalidPropertyException.class,
                () -> Property.getProperty("key"));
        assertEquals("Property must contain '=': key", exception.getMessage(),
                "Should throw InvalidPropertyException for missing '='");
    }

    @Test
    void testGetProperty_EmptyName() {
        InvalidPropertyException exception = assertThrows(InvalidPropertyException.class,
                () -> Property.getProperty("=value"));
        assertEquals("Property name cannot be empty: =value", exception.getMessage(),
                "Should throw InvalidPropertyException for empty name");
    }

    @Test
    void testEmptyConstructor() {
        Property prop = new Property();
        assertNull(prop.getName(), "Name should be null");
        assertNull(prop.getValue(), "Value should be null");
        assertNull(prop.getValues(), "Values should be null");
        assertFalse(prop.isMulti(), "Should not be multi-valued");
    }

    @Test
    void testSingleValueConstructor() {
        Property prop = new Property("name", "value");
        assertEquals("name", prop.getName(), "Name should be 'name'");
        assertEquals("value", prop.getValue(), "Value should be 'value'");
        assertNull(prop.getValues(), "Values should be null");
        assertFalse(prop.isMulti(), "Should not be multi-valued");
    }

    @Test
    void testMultiValueConstructor() {
        String[] values = {"val1", "val2"};
        Property prop = new Property("name", values);
        assertEquals("name", prop.getName(), "Name should be 'name'");
        assertNull(prop.getValue(), "Value should be null");
        assertTrue(Arrays.equals(values, prop.getValues()), "Values should match");
        assertTrue(prop.isMulti(), "Should be multi-valued");
    }

    @Test
    void testGetSetName() {
        property.setName("key");
        assertEquals("key", property.getName(), "Name should be 'key'");
        property.setName(null);
        assertNull(property.getName(), "Name should be null");
    }

    @Test
    void testGetSetValue() {
        property.setValue("value");
        assertEquals("value", property.getValue(), "Value should be 'value'");
        property.setValue(null);
        assertNull(property.getValue(), "Value should be null");
    }

    @Test
    void testGetSetValues() {
        String[] values = {"val1", "val2"};
        property.setValues(values);
        assertTrue(Arrays.equals(values, property.getValues()), "Values should match");
        assertTrue(property.isMulti(), "Should be multi-valued after setValues");
        property.setValues(null);
        assertNull(property.getValues(), "Values should be null");
        assertTrue(property.isMulti(), "isMulti should remain true");
        property.setValues(new String[]{});
        assertTrue(Arrays.equals(new String[]{}, property.getValues()), "Values should be empty array");
        assertTrue(property.isMulti(), "isMulti should remain true");
    }

    @Test
    void testGetSetIsMulti() {
        property.setMulti(true);
        assertTrue(property.isMulti(), "Should be multi-valued");
        property.setMulti(false);
        assertFalse(property.isMulti(), "Should not be multi-valued");
    }

}
