package co.acu.pagetool.crx;

import co.acu.pagetool.OperationProperties;
import co.acu.pagetool.PageToolApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryUrlTest {

    @Mock private CrxConnection conn;
    @Mock private Property property;
    @Mock private OperationProperties opProps;

    @InjectMocks private QueryUrl queryUrl;

    @BeforeEach
    void setUp() {
        PageToolApp.verbose = false;
    }

    private void setupConnStubs() {
        when(conn.isSecure()).thenReturn(false);
        when(conn.getHostname()).thenReturn("localhost");
        when(conn.getPort()).thenReturn("4502");
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = QueryUrl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(queryUrl, value);
    }

    @Test
    void testBuildUrl_QuerySimplePath() {
        setupConnStubs();
        String path = "/content/test";
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10";
        String result = queryUrl.buildUrl(path, null, null, true, false, null, false, null);
        assertEquals(expected, result, "Query URL should match expected format");
    }

    @Test
    void testBuildUrl_QueryWithSingleProperty() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.getValue()).thenReturn("test1");
        when(property.isMulti()).thenReturn(false);
        List<Property> properties = new ArrayList<>(Collections.singletonList(property));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&1_property=maSortingTag&1_property.value=test1";
        String result = queryUrl.buildUrl(path, properties, null, true, false, null, false, null);
        assertEquals(expected, result, "Query URL with single property should match");
    }

    @Test
    void testBuildUrl_QueryWithMultiValuedProperty() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.isMulti()).thenReturn(true);
        when(property.getValues()).thenReturn(new String[]{"test1"});
        List<Property> properties = new ArrayList<>(Collections.singletonList(property));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&1_property=maSortingTag&1_property.operation=like&1_property.value=%25test1%25";
        String result = queryUrl.buildUrl(path, properties, null, true, false, null, false, null);
        assertEquals(expected, result, "Query URL with multi-valued property should match");
    }

    @Test
    void testBuildUrl_QueryWithNode() {
        setupConnStubs();
        String path = "/content/test";
        List<String> nodes = new ArrayList<>(Collections.singletonList("newNode"));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&nodename=newNode";
        String result = queryUrl.buildUrl(path, null, nodes, true, false, null, false, null);
        assertEquals(expected, result, "Query URL with node should match");
    }

    @Test
    void testBuildUrl_QueryWithCqPageType() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.getValue()).thenReturn("test1");
        when(property.isMulti()).thenReturn(false);
        List<Property> properties = new ArrayList<>(Collections.singletonList(property));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&1_property=maSortingTag&1_property.value=test1&type=cq:PageContent";
        String result = queryUrl.buildUrl(path, properties, null, true, true, null, false, null);
        assertEquals(expected, result, "Query URL with cqPageType should include type=cq:PageContent");
    }

    @Test
    void testBuildUrl_QueryWithPropertyCopy() {
        setupConnStubs();
        String path = "/content/test";
        when(opProps.getCopyFromProperties()).thenReturn(new ArrayList<>(Collections.singletonList("bgPageImage")));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=bgPageImage&property.operation=exists";
        String result = queryUrl.buildUrl(path, null, null, true, false, null, true, opProps);
        assertEquals(expected, result, "Query URL with property copy should include source property filter");
    }

    @Test
    void testBuildUrl_QueryWithPropertyCopyAndCqPageType() {
        setupConnStubs();
        String path = "/content/test";
        when(opProps.getCopyFromProperties()).thenReturn(new ArrayList<>(Collections.singletonList("bgPageImage")));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=bgPageImage&property.operation=exists&type=cq:PageContent";
        String result = queryUrl.buildUrl(path, null, null, true, true, null, true, opProps);
        assertEquals(expected, result, "Query URL with property copy and cqPageType should include source property filter and type=cq:PageContent");
    }

    @Test
    void testBuildUrl_QueryWithPropertyCopyNestedProperty() {
        setupConnStubs();
        String path = "/content/test";
        when(opProps.getCopyFromProperties()).thenReturn(new ArrayList<>(Collections.singletonList("par/subpar/prop1")));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=par/subpar/prop1&property.operation=exists";
        String result = queryUrl.buildUrl(path, null, null, true, false, null, true, opProps);
        assertEquals(expected, result, "Query URL with nested property copy should include property filter");
    }

    @Test
    void testBuildUrl_UpdateSimplePath() {
        setupConnStubs();
        String path = "/content/test";
        String expected = "http://localhost:4502/content/test.json";
        String result = queryUrl.buildUrl(path, null, null, false, false, null, false, null);
        assertEquals(expected, result, "Update URL should match");
    }

    @Test
    void testBuildUrl_UpdateWithCqPageType() {
        setupConnStubs();
        String path = "/content/test";
        String expected = "http://localhost:4502/content/test/jcr:content.json";
        String result = queryUrl.buildUrl(path, null, null, false, true, null, false, null);
        assertEquals(expected, result, "Update URL with cqPageType should include jcr:content");
    }

    @Test
    void testBuildUrl_UpdateWithCopyProperty() {
        setupConnStubs();
        String path = "/content/test";
        String copyProperty = "sourceProp";
        String expected = "http://localhost:4502/content/test/sourceProp.json";
        String result = queryUrl.buildUrl(path, null, null, false, false, copyProperty, false, null);
        assertEquals(expected, result, "Update URL with copy property should match");
    }

    @Test
    void testBuildUrl_SecondOverload() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = new ArrayList<>();
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10";
        String result = queryUrl.buildUrl(path, properties, null, false, false, null);
        assertEquals(expected, result, "Second overload should build query URL");
    }

    @Test
    void testBuildUrl_SecondOverloadWithPropertyCopy() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = new ArrayList<>();
        when(opProps.getCopyFromProperties()).thenReturn(new ArrayList<>(Collections.singletonList("bgPageImage")));
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=bgPageImage&property.operation=exists";
        String result = queryUrl.buildUrl(path, properties, null, false, true, opProps);
        assertEquals(expected, result, "Second overload with property copy should include source property filter");
    }

    @Test
    void testBuildUrl_ThirdOverloadQuery() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = new ArrayList<>();
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10";
        String result = queryUrl.buildUrl(true, path, properties);
        assertEquals(expected, result, "Third overload for query should match");
    }

    @Test
    void testBuildUrl_ThirdOverloadUpdate() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = new ArrayList<>();
        String expected = "http://localhost:4502/content/test.json";
        String result = queryUrl.buildUrl(false, path, properties);
        assertEquals(expected, result, "Third overload for update should match");
    }

    @Test
    void testBuildUrl_FourthOverload() {
        setupConnStubs();
        String path = "/content/test";
        String copyProperty = "sourceProp";
        String expected = "http://localhost:4502/content/test/sourceProp.json";
        String result = queryUrl.buildUrl(path, copyProperty);
        assertEquals(expected, result, "Fourth overload should build update URL with copy property");
    }

    @Test
    void testConstructor_NullCrxConnection() {
        Exception exception = assertThrows(NullPointerException.class, () -> new QueryUrl(null));
        assertEquals("CrxConnection must not be null", exception.getMessage(), "Constructor should throw NPE for null connection");
    }

}
