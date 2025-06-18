package co.acu.pagetool;

import co.acu.pagetool.crx.Property;
import co.acu.pagetool.exception.InvalidPropertyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationPropertiesTest {

    @Mock private Property property;

    private OperationProperties operationProperties;

    @BeforeEach
    void setUp() {
        PageToolApp.verbose = false;
        operationProperties = new OperationProperties();
    }

    @Test
    void testSetMatchingProperties_StringArray_Valid() throws InvalidPropertyException {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("name=value")).thenReturn(property);

            operationProperties.setMatchingProperties(new String[]{"name=value"});
            ArrayList<Property> result = operationProperties.getMatchingProperties();

            assertEquals(1, result.size(), "Should have one matching property");
            assertEquals(property, result.get(0), "Property should match mocked instance");
        }
    }

    @Test
    void testSetMatchingProperties_StringArray_JcrContent() throws InvalidPropertyException {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("jcr:primaryType=cq:PageContent")).thenReturn(property);

            operationProperties.setMatchingProperties(new String[]{"jcr:content=cq:PageContent"});
            ArrayList<Property> result = operationProperties.getMatchingProperties();
            ArrayList<String> nodes = operationProperties.getMatchingNodes();

            assertEquals(1, result.size(), "Should have one matching property");
            assertEquals(property, result.get(0), "Property should match jcr:primaryType");
            assertEquals(Collections.singletonList("jcr:content"), nodes, "Should add jcr:content to matching nodes");
        }
    }

    @Test
    void testSetMatchingProperties_StringArray_Invalid() {
        InvalidPropertyException exception = assertThrows(InvalidPropertyException.class,
                () -> operationProperties.setMatchingProperties(new String[]{"invalid"}));
        assertEquals("Property must contain '=': invalid", exception.getMessage(),
                "Should throw InvalidPropertyException for invalid property");
    }

    @Test
    void testSetSearchValue_Valid() {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("name=[value]")).thenReturn(property);
            when(property.getValue()).thenReturn("[value]");
            when(property.isMulti()).thenReturn(false, true);

            operationProperties.setSearchValue(new String[]{"name=[value]", "node1"});
            ArrayList<Property> result = operationProperties.getMatchingProperties();
            ArrayList<String> nodes = operationProperties.getMatchingNodes();

            assertEquals(1, result.size(), "Should have one matching property");
            assertTrue(result.get(0).isMulti(), "Property should be marked as multi-valued");
            verify(property).setValues(new String[]{"[value]"});
            assertEquals(Collections.singletonList("node1"), nodes, "Should add node1 to matching nodes");
            assertTrue(operationProperties.isSearchOnly(), "Should be search only");
        }
    }

    @Test
    void testSetSearchValue_Null() {
        operationProperties.setSearchValue(null);
        assertNull(operationProperties.getMatchingProperties(), "Matching properties should remain null");
        assertNull(operationProperties.getMatchingNodes(), "Matching nodes should remain null");
    }

    @Test
    void testSetUpdateProperties_StringArray_Valid() throws InvalidPropertyException {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("name=value")).thenReturn(property);

            operationProperties.setUpdateProperties(new String[]{"name=value"});
            ArrayList<Property> result = operationProperties.getUpdateProperties();

            assertEquals(1, result.size(), "Should have one update property");
            assertEquals(property, result.get(0), "Property should match mocked instance");
            assertFalse(operationProperties.isSearchOnly(), "Should not be search only");
        }
    }

    @Test
    void testSetPropertyValueReplacement_Valid() throws Exception {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("name=old")).thenReturn(property);
            when(property.getValue()).thenReturn("old");

            operationProperties.setPropertyValueReplacement(new String[]{"name=old"}, new String[]{"new"});
            Property result = operationProperties.getPropertyValueReplacement();
            ArrayList<Property> resultList = operationProperties.getPropertyValueReplacementAsList();

            assertEquals(property, result, "Property should match mocked instance");
            verify(property).setValues(new String[]{"old", "new"});
            assertEquals(1, resultList.size(), "List should contain one property");
            assertEquals(property, resultList.get(0), "List property should match");
        }
    }

    @Test
    void testSetCopyFromProperties_Valid() {
        operationProperties.setCopyFromProperties(new String[]{"prop1", "prop2"});
        ArrayList<String> result = operationProperties.getCopyFromProperties();

        assertEquals(Arrays.asList("prop1", "prop2"), result, "Should set copy from properties");
    }

    @Test
    void testSetCopyToProperties_Valid() {
        operationProperties.setCopyToProperties(new String[]{"prop1", "prop2"});
        ArrayList<String> result = operationProperties.getCopyToProperties();

        assertEquals(Arrays.asList("prop1", "prop2"), result, "Should set copy to properties");
    }

    @Test
    void testSetDeleteProperties_StringArray_Valid() throws InvalidPropertyException {
        operationProperties.setDeleteProperties(new String[]{"prop1", "prop2"});
        ArrayList<String> result = operationProperties.getDeleteProperties();

        assertEquals(Arrays.asList("prop1", "prop2"), result, "Should set delete properties");
    }

    @Test
    void testSetCreateNode_Valid() throws InvalidPropertyException {
        try (MockedStatic<Property> staticMock = mockStatic(Property.class)) {
            staticMock.when(() -> Property.getProperty("node=nt:unstructured")).thenReturn(property);

            operationProperties.setCreateNode(new String[]{"node=nt:unstructured"});
            Property result = operationProperties.getCreateNode();

            assertEquals(property, result, "Node property should match mocked instance");
            assertFalse(operationProperties.isSearchOnly(), "Should not be search only");
        }
    }

    @Test
    void testSetCreateNode_Invalid() {
        InvalidPropertyException exception = assertThrows(InvalidPropertyException.class,
                () -> operationProperties.setCreateNode(new String[]{"invalid"}));
        assertEquals("Property must contain '=': invalid", exception.getMessage(),
                "Should throw InvalidPropertyException for invalid node");
    }

    @Test
    void testSetCqPageType() {
        operationProperties.setCqPageType(true);
        assertTrue(operationProperties.isCqPageType(), "CqPageType should be true");
    }

    @Test
    void testGetters_NullInitialState() {
        assertNull(operationProperties.getMatchingProperties(), "Matching properties should be null initially");
        assertNull(operationProperties.getMatchingNodes(), "Matching nodes should be null initially");
        assertNull(operationProperties.getUpdateProperties(), "Update properties should be null initially");
        assertNull(operationProperties.getPropertyValueReplacement(), "Property value replacement should be null initially");
        assertNull(operationProperties.getPropertyValueReplacementAsList(), "Property value replacement list should be null initially");
        assertNull(operationProperties.getCopyFromProperties(), "Copy from properties should be null initially");
        assertNull(operationProperties.getCopyToProperties(), "Copy to properties should be null initially");
        assertNull(operationProperties.getDeleteProperties(), "Delete properties should be null initially");
        assertNull(operationProperties.getCreateNode(), "Create node should be null initially");
        assertFalse(operationProperties.isSearchOnly(), "Search only should be false initially");
    }

}
