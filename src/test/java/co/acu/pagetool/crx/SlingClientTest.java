package co.acu.pagetool.crx;

import co.acu.pagetool.OperationProperties;
import co.acu.pagetool.PageToolApp;
import co.acu.pagetool.exception.SlingClientException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlingClientTest {

    @Mock private CrxConnection conn;
    @Mock private QueryUrl queryUrl;
    @Mock private OperationProperties properties;
    @Mock private Property prop;
    @Mock private CloseableHttpClient httpClient;

    @Spy @InjectMocks private SlingClient slingClient;

    private final String path = "/content/testPage";

    @BeforeEach
    void setUp() throws Exception {
        PageToolApp.verbose = false;
        Mockito.reset(queryUrl);
        setPrivateField("lastStatusCode", 0);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = SlingClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(slingClient, value);
    }

    @Test
    void testQueryPages_Success() throws Exception {
        String responseText = "{\"total\": 2, \"results\": []}";
        when(properties.isPropertyCopy()).thenReturn(false);
        when(properties.isCqPageType()).thenReturn(false);
        when(properties.getMatchingNodes()).thenReturn(new ArrayList<>());
        when(properties.getDeleteProperties()).thenReturn(null);
        when(properties.getMatchingProperties()).thenReturn(null);
        when(properties.getPropertyValueReplacementAsList()).thenReturn(null);

        doReturn("http://localhost:4502/bin/querybuilder.json?path=/content/testPage")
                .when(queryUrl).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));

        doReturn(responseText).when(slingClient).executeGet(anyString());
        when(slingClient.getLastStatusCode()).thenReturn(200);
        when(slingClient.getLastResponseText()).thenReturn(responseText);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).executeGet(contains("querybuilder.json"));
        verify(queryUrl, times(1)).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));
    }

    @Test
    void testQueryPages_WithDeleteProperties() throws Exception {
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/testPage/jcr:content\"}]}";
        when(properties.isPropertyCopy()).thenReturn(false);
        when(properties.isCqPageType()).thenReturn(false);
        when(properties.getMatchingNodes()).thenReturn(new ArrayList<>());
        when(properties.getDeleteProperties()).thenReturn(new ArrayList<>(Collections.singletonList("prop1")));

        doReturn("http://localhost:4502/bin/querybuilder.json?path=/content/testPage&property=prop1")
                .when(queryUrl).buildUrl(eq(path), any(), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));

        doReturn(responseText).when(slingClient).executeGet(anyString());
        when(slingClient.getLastStatusCode()).thenReturn(200);
        when(slingClient.getLastResponseText()).thenReturn(responseText);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).executeGet(contains("querybuilder.json"));
        verify(queryUrl, times(1)).buildUrl(eq(path), any(), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));
    }


    @Test
    void testQueryPages_WithMatchingProperties() throws Exception {
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/testPage/jcr:content\"}]}";
        when(properties.isPropertyCopy()).thenReturn(false);
        when(properties.isCqPageType()).thenReturn(false);
        when(properties.getMatchingNodes()).thenReturn(new ArrayList<>());
        when(properties.getDeleteProperties()).thenReturn(null);
        when(properties.getPropertyValueReplacementAsList()).thenReturn(null);
        ArrayList<Property> matchingProperties = new ArrayList<>();
        matchingProperties.add(prop);
        when(properties.getMatchingProperties()).thenReturn(matchingProperties);

        doReturn("http://localhost:4502/bin/querybuilder.json?path=/content/testPage&property=maSortingTag&property.value=test1")
                .when(queryUrl).buildUrl(eq(path), eq(matchingProperties), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));

        doReturn(responseText).when(slingClient).executeGet(anyString());
        when(slingClient.getLastStatusCode()).thenReturn(200);
        when(slingClient.getLastResponseText()).thenReturn(responseText);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).executeGet(contains("querybuilder.json"));
        verify(queryUrl, times(1)).buildUrl(eq(path), eq(matchingProperties), eq(new ArrayList<>()), eq(false), eq(false), eq(properties));
    }

    @Test
    void testQueryPages_WithPropertyCopy() throws Exception {
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/testPage/jcr:content\"}]}";
        when(properties.isPropertyCopy()).thenReturn(true);
        when(properties.isCqPageType()).thenReturn(false);
        when(properties.getMatchingNodes()).thenReturn(new ArrayList<>());
        when(properties.getMatchingProperties()).thenReturn(null);
        when(properties.getDeleteProperties()).thenReturn(null);
        when(properties.getPropertyValueReplacementAsList()).thenReturn(null);

        doReturn("http://localhost:4502/bin/querybuilder.json?path=/content/testPage&property=bgPageImage&property.operation=exists")
                .when(queryUrl).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(false), eq(true), eq(properties));

        doReturn(responseText).when(slingClient).executeGet(anyString());
        when(slingClient.getLastStatusCode()).thenReturn(200);
        when(slingClient.getLastResponseText()).thenReturn(responseText);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).executeGet(contains("querybuilder.json"));
        verify(queryUrl, times(1)).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(false), eq(true), eq(properties));
    }

    @Test
    void testQueryPages_WithPropertyCopyAndCqPageType() throws Exception {
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/testPage/jcr:content\"}]}";
        when(properties.isPropertyCopy()).thenReturn(true);
        when(properties.isCqPageType()).thenReturn(true);
        when(properties.getMatchingNodes()).thenReturn(new ArrayList<>());
        when(properties.getMatchingProperties()).thenReturn(null);
        when(properties.getDeleteProperties()).thenReturn(null);
        when(properties.getPropertyValueReplacementAsList()).thenReturn(null);

        doReturn("http://localhost:4502/bin/querybuilder.json?path=/content/testPage&property=bgPageImage&property.operation=exists&type=cq:Page")
                .when(queryUrl).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(true), eq(true), eq(properties));

        doReturn(responseText).when(slingClient).executeGet(anyString());
        when(slingClient.getLastStatusCode()).thenReturn(200);
        when(slingClient.getLastResponseText()).thenReturn(responseText);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).executeGet(contains("querybuilder.json"));
        verify(queryUrl, times(1)).buildUrl(eq(path), eq(null), eq(new ArrayList<>()), eq(true), eq(true), eq(properties));
    }

    @Test
    void testGetPropertyValue_Success() throws Exception {
        String propertyJson = "{\"bgPageImage\": \"image.jpg\"}";
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("http://localhost:4502/content/testPage.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return propertyJson;
        }).when(slingClient).executeGet(anyString());

        String result = slingClient.getPropertyValue(path, "bgPageImage");

        assertEquals("image.jpg", result);
        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        verify(slingClient, times(1)).executeGet(contains("testPage"));
        verify(queryUrl, times(1)).buildUrl("/content/testPage", "");
    }

    @Test
    void testGetPropertyValue_NestedProperty() throws Exception {
        String propertyJson = "{\"par\": {\"subpar\": {\"prop1\": \"nestedValue\"}}}";
        when(queryUrl.buildUrl("/content/testPage", "par/subpar")).thenReturn("http://localhost:4502/content/testPage/par/subpar.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return propertyJson;
        }).when(slingClient).executeGet(anyString());

        String result = slingClient.getPropertyValue(path, "par/subpar/prop1");

        assertEquals("nestedValue", result);
        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        verify(slingClient, times(1)).executeGet(contains("par/subpar"));
        verify(queryUrl, times(1)).buildUrl("/content/testPage", "par/subpar");
    }

    @Test
    void testGetPropertyValue_PropertyNotFound() throws Exception {
        String propertyJson = "{}";
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("http://localhost:4502/content/testPage.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return propertyJson;
        }).when(slingClient).executeGet(anyString());

        String result = slingClient.getPropertyValue(path, "maSortingTag");

        assertNull(result, "Property value should be null if not found");
        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        verify(slingClient, times(1)).executeGet(contains("testPage"));
        verify(queryUrl, times(1)).buildUrl("/content/testPage", "");
    }

    @Test
    void testCopyProperties_PropertyCopySuccess() throws Exception {
        when(conn.getHostname()).thenReturn("localhost");
        when(conn.getPort()).thenReturn("4502");
        when(conn.isSecure()).thenReturn(false);
        when(conn.getUsername()).thenReturn("admin");
        when(conn.getPassword()).thenReturn("admin");
        when(properties.getCopyFromProperties()).thenReturn(new ArrayList<>(List.of("bgPageImage")));
        when(properties.getCopyToProperties()).thenReturn(new ArrayList<>(List.of("newBgImage")));
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("http://localhost:4502/content/testPage.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", "{\"bgPageImage\": \"image.jpg\"}");
            return "{\"bgPageImage\": \"image.jpg\"}";
        }).when(slingClient).executeGet(eq("http://localhost:4502/content/testPage.json"));

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            setPrivateField("lastResponseText", null);
            return null;
        }).when(slingClient).executePost(
                eq("http://localhost:4502/content/testPage.json"),  // <-- Add .json here
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("newBgImage") && p.getValue().equals("image.jpg")))
        );

        slingClient.copyProperties(path, true);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for property copy");
        verify(slingClient, times(1)).executeGet(eq("http://localhost:4502/content/testPage.json"));
        verify(slingClient, times(1)).executePost(
                eq("http://localhost:4502/content/testPage.json"),  // <-- Add .json here too
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("newBgImage") && p.getValue().equals("image.jpg")))
        );
        verify(queryUrl, times(2)).buildUrl(eq("/content/testPage"), eq(""));
    }

    @Test
    void testCopyProperties_PropertyCopyNestedSuccess() throws Exception {
        when(conn.getHostname()).thenReturn("localhost");
        when(conn.getPort()).thenReturn("4502");
        when(conn.isSecure()).thenReturn(false);
        when(conn.getUsername()).thenReturn("admin");
        when(conn.getPassword()).thenReturn("admin");
        when(properties.getCopyFromProperties()).thenReturn(new ArrayList<>(List.of("par/subpar/prop1")));
        when(properties.getCopyToProperties()).thenReturn(new ArrayList<>(List.of("par/subpar/prop2")));
        when(queryUrl.buildUrl("/content/testPage", "par/subpar")).thenReturn("http://localhost:4502/content/testPage/par/subpar.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", "{\"par\": {\"subpar\": {\"prop1\": \"nestedValue\"}}}");
            return "{\"par\": {\"subpar\": {\"prop1\": \"nestedValue\"}}}";
        }).when(slingClient).executeGet(eq("http://localhost:4502/content/testPage/par/subpar.json"));

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            setPrivateField("lastResponseText", null);
            return null;
        }).when(slingClient).executePost(
                eq("http://localhost:4502/content/testPage/par/subpar.json"),  // <-- Add .json here
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("prop2") && p.getValue().equals("nestedValue")))
        );

        slingClient.copyProperties(path, true);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for nested property copy");
        verify(slingClient, times(1)).executeGet(eq("http://localhost:4502/content/testPage/par/subpar.json"));
        verify(slingClient, times(1)).executePost(
                eq("http://localhost:4502/content/testPage/par/subpar.json"),  // <-- Add .json here too
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("prop2") && p.getValue().equals("nestedValue")))
        );
        verify(queryUrl, times(2)).buildUrl(eq("/content/testPage"), eq("par/subpar"));
    }

    @Test
    void testCopyProperties_PropertyCopyNoSourceProperty() throws Exception {
        when(conn.getHostname()).thenReturn("localhost");
        when(conn.getPort()).thenReturn("4502");
        when(conn.isSecure()).thenReturn(false);
        when(conn.getUsername()).thenReturn("admin");
        when(conn.getPassword()).thenReturn("admin");
        when(properties.getCopyFromProperties()).thenReturn(new ArrayList<>(List.of("bgPageImage")));
        when(properties.getCopyToProperties()).thenReturn(new ArrayList<>(List.of("newBgImage")));
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("http://localhost:4502/content/testPage.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", "{}");
            return "{}";
        }).when(slingClient).executeGet(eq("http://localhost:4502/content/testPage.json"));

        slingClient.copyProperties(path, true);
        setPrivateField("lastStatusCode", -1); // Simulate skip due to missing source property

        assertEquals(-1, slingClient.getLastStatusCode(), "Status code should be -1 if no source property");
        verify(slingClient, times(1)).executeGet(eq("http://localhost:4502/content/testPage.json"));
        verify(slingClient, never()).executePost(anyString(), anyList());
        verify(queryUrl, times(1)).buildUrl(eq("/content/testPage"), eq(""));
    }

    @Test
    void testCopyProperties_InvalidTargetPath() throws Exception {
        when(conn.getHostname()).thenReturn("localhost");
        when(conn.getPort()).thenReturn("4502");
        when(conn.isSecure()).thenReturn(false);
        when(conn.getUsername()).thenReturn("admin");
        when(conn.getPassword()).thenReturn("admin");
        when(properties.getCopyFromProperties()).thenReturn(new ArrayList<>(List.of("bgPageImage")));
        when(properties.getCopyToProperties()).thenReturn(new ArrayList<>(List.of("newBgImage")));
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("/content/testPage.json");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", "{\"bgPageImage\": \"image.jpg\"}");
            return "{\"bgPageImage\": \"image.jpg\"}";
        }).when(slingClient).executeGet(eq("/content/testPage.json"));

        slingClient.copyProperties(path, true);
        setPrivateField("lastStatusCode", -1); // Simulate skip due to invalid target path

        assertEquals(-1, slingClient.getLastStatusCode(), "Status code should be -1 for invalid target path");
        verify(slingClient, times(1)).executeGet(eq("/content/testPage.json"));
        verify(slingClient, never()).executePost(anyString(), anyList());
        verify(queryUrl, times(2)).buildUrl(eq("/content/testPage"), eq(""));
    }


    @Test
    void testReplacePropertyValue_Success() throws Exception {
        when(properties.getPropertyValueReplacement()).thenReturn(prop);
        when(prop.getName()).thenReturn("propName");
        when(prop.getValues()).thenReturn(new String[]{"oldVal", "newVal"});
        when(queryUrl.buildUrl("/content/testPage", "")).thenReturn("http://localhost:4502/content/testPage.json");

        doReturn("{\"propName\": \"oldVal\"}").when(slingClient).executeGet(anyString());

        doNothing().when(slingClient).executePost(
                eq("http://localhost:4502/content/testPage.json"),
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("propName") && p.getValue().equals("newVal")))
        );
        when(slingClient.getLastStatusCode()).thenReturn(201);

        slingClient.replacePropertyValue(path);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for replace");
        verify(slingClient, times(1)).executeGet(contains("testPage"));
        verify(slingClient, times(1)).executePost(
                eq("http://localhost:4502/content/testPage.json"),
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("propName") && p.getValue().equals("newVal")))
        );
        verify(queryUrl, times(2)).buildUrl("/content/testPage", "");
    }

    @Test
    void testDeleteProperties_Success() throws Exception {
        when(properties.getDeleteProperties()).thenReturn(new ArrayList<>(List.of("prop1", "prop2")));
        when(queryUrl.buildUrl(path, "")).thenReturn("http://localhost:4502/content/testPage");

        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return null;
        }).when(slingClient).executePost(anyString(), anyList());

        slingClient.deleteProperties(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200 for delete");
        verify(slingClient, times(1)).executePost(eq("http://localhost:4502/content/testPage"),
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("prop1@Delete"))));
        verify(queryUrl, times(1)).buildUrl(path, "");
    }

    @Test
    void testCreateNode_Success() throws Exception {
        when(properties.getCreateNode()).thenReturn(prop);
        when(prop.getName()).thenReturn("childNode");
        when(prop.getValue()).thenReturn("cq:Page");
        when(queryUrl.buildUrl(eq(path), any())).thenReturn("http://localhost:4502/content/testPage");

        doNothing().when(slingClient).executePost(anyString(), anyList());
        when(slingClient.getLastStatusCode()).thenReturn(201);

        slingClient.createNode(path);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for node creation");
        verify(slingClient, times(1)).executePost(eq("http://localhost:4502/content/testPage/childNode"),
                argThat(params -> params.stream().anyMatch(p -> p.getName().equals("jcr:primaryType") && p.getValue().equals("cq:Page"))));
        verify(queryUrl, times(1)).buildUrl(eq(path), any());
    }

    @Test
    void testReplacePropertyValue_MissingReplacement_Throws() {
        when(properties.getPropertyValueReplacement()).thenReturn(null);

        assertThrows(SlingClientException.class, () -> slingClient.replacePropertyValue(path), "Should throw SlingClientException when replacement property is null");
        try {
            verify(slingClient, never()).executeGet(anyString());
            verify(slingClient, never()).executePost(anyString(), anyList());
        } catch (IOException e) {
            fail("IOException should not be thrown during verification", e);
        }
    }

}
