package co.acu.pagetool.crx;

import co.acu.pagetool.PageToolApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryUrlTest {

    @Mock private CrxConnection conn;
    @Mock private Property property;

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
        String result = queryUrl.buildUrl(path, null, null, true, false, null);
        assertEquals(expected, result, "Query URL should match expected format");
    }

    @Test
    void testBuildUrl_QueryWithSingleProperty() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.getValue()).thenReturn("test1");
        when(property.isMulti()).thenReturn(false);
        List<Property> properties = Collections.singletonList(property);
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=maSortingTag&property.value=test1";
        String result = queryUrl.buildUrl(path, properties, null, true, false, null);
        assertEquals(expected, result, "Query URL with single property should match");
    }

    @Test
    void testBuildUrl_QueryWithMultiValuedProperty() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.isMulti()).thenReturn(true);
        when(property.getValues()).thenReturn(new String[]{"test1"});
        List<Property> properties = Collections.singletonList(property);
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=maSortingTag&property.operation=contains&property.value=test1";
        String result = queryUrl.buildUrl(path, properties, null, true, false, null);
        assertEquals(expected, result, "Query URL with multi-valued property should match");
    }

    @Test
    void testBuildUrl_QueryWithNode() {
        setupConnStubs();
        String path = "/content/test";
        List<String> nodes = Collections.singletonList("newNode");
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&nodename=newNode";
        String result = queryUrl.buildUrl(path, null, nodes, true, false, null);
        assertEquals(expected, result, "Query URL with node should match");
    }

    @Test
    void testBuildUrl_QueryWithCqPageType() {
        setupConnStubs();
        String path = "/content/test";
        when(property.getName()).thenReturn("maSortingTag");
        when(property.getValue()).thenReturn("test1");
        when(property.isMulti()).thenReturn(false);
        List<Property> properties = Collections.singletonList(property);
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10&property=jcr:content/maSortingTag&property.value=test1";
        String result = queryUrl.buildUrl(path, properties, null, true, true, null);
        assertEquals(expected, result, "Query URL with cqPageType should include jcr:content");
    }

    @Test
    void testBuildUrl_QueryWithTwoNodesDepth() throws Exception {
        setupConnStubs();
        String path = "/content/test";
        setPrivateField("nodeDepth", 1);
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=1";
        String result = queryUrl.buildUrl(path, null, null, true, false, null);
        assertEquals(expected, result, "Query URL with nodeDepth=1 should limit to two nodes");
    }

    @Test
    void testBuildUrl_UpdateSimplePath() {
        setupConnStubs();
        String path = "/content/test";
        String expected = "http://localhost:4502/content/test";
        String result = queryUrl.buildUrl(path, null, null, false, false, null);
        assertEquals(expected, result, "Update URL should match");
    }

    @Test
    void testBuildUrl_UpdateWithCqPageType() {
        setupConnStubs();
        String path = "/content/test";
        String expected = "http://localhost:4502/content/test/jcr:content";
        String result = queryUrl.buildUrl(path, null, null, false, true, null);
        assertEquals(expected, result, "Update URL with cqPageType should include jcr:content");
    }

    @Test
    void testBuildUrl_UpdateWithCopyProperty() {
        setupConnStubs();
        String path = "/content/test";
        String copyProperty = "sourceProp";
        String expected = "http://localhost:4502/content/test/sourceProp";
        String result = queryUrl.buildUrl(path, null, null, false, false, copyProperty);
        assertEquals(expected, result, "Update URL with copy property should match");
    }

    @Test
    void testBuildUrl_SecondOverload() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = Collections.emptyList();
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10";
        String result = queryUrl.buildUrl(path, properties, null, false);
        assertEquals(expected, result, "Second overload should build query URL");
    }

    @Test
    void testBuildUrl_ThirdOverloadQuery() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = Collections.emptyList();
        String expected = "http://localhost:4502/bin/querybuilder.json?path=/content/test&p.limit=1000&p.hits=selective&p.properties=jcr:path&p.nodedepth=10";
        String result = queryUrl.buildUrl(true, path, properties);
        assertEquals(expected, result, "Third overload for query should match");
    }

    @Test
    void testBuildUrl_ThirdOverloadUpdate() {
        setupConnStubs();
        String path = "/content/test";
        List<Property> properties = Collections.emptyList();
        String expected = "http://localhost:4502/content/test";
        String result = queryUrl.buildUrl(false, path, properties);
        assertEquals(expected, result, "Third overload for update should match");
    }

    @Test
    void testBuildUrl_FourthOverload() {
        setupConnStubs();
        String path = "/content/test";
        String copyProperty = "sourceProp";
        String expected = "http://localhost:4502/content/test/sourceProp";
        String result = queryUrl.buildUrl(path, copyProperty);
        assertEquals(expected, result, "Fourth overload should build update URL with copy property");
    }

    @Test
    void testConstructor_NullCrxConnection() {
        Exception exception = assertThrows(NullPointerException.class, () -> new QueryUrl(null));
        assertEquals("CrxConnection must not be null", exception.getMessage(), "Constructor should throw NPE for null connection");
    }

}
