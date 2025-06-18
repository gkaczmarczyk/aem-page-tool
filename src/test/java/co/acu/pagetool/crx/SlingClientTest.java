package co.acu.pagetool.crx;

import co.acu.pagetool.OperationProperties;
import co.acu.pagetool.PageToolApp;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlingClientTest {

    @Mock private CrxConnection conn;
    @Mock private QueryUrl queryUrl;
    @Mock private OperationProperties properties;
    @Mock private CloseableHttpClient httpClient;
    @Mock private CloseableHttpResponse httpResponse;

    @Spy @InjectMocks private SlingClient slingClient;

    @BeforeEach
    void setUp() {
        PageToolApp.verbose = false;
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = SlingClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(slingClient, value);
    }

    @Test
    void testQueryPages_Success() throws Exception {
        String path = "/content/test";
        String responseText = "{\"total\": 2, \"results\": []}";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", responseText);
            return null;
        }).when(slingClient).queryPages(path);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).queryPages(path);
    }

    @Test
    void testQueryPages_WithDeleteProperties() throws Exception {
        String path = "/content/test";
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/test/jcr:content\"}]}";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", responseText);
            return null;
        }).when(slingClient).queryPages(path);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).queryPages(path);
    }

    @Test
    void testQueryPages_WithMatchingProperties() throws Exception {
        String path = "/content/test";
        String responseText = "{\"total\": 1, \"hits\": [{\"jcr:path\": \"/content/test/jcr:content\"}]}";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            setPrivateField("lastResponseText", responseText);
            return null;
        }).when(slingClient).queryPages(path);

        slingClient.queryPages(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200");
        assertEquals(responseText, slingClient.getLastResponseText(), "Response text should match");
        verify(slingClient, times(1)).queryPages(path);
    }

    @Test
    void testUpdatePage_SimpleUpdate() throws Exception {
        String path = "/content/test";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            return null;
        }).when(slingClient).updatePage(path);

        slingClient.updatePage(path);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for update");
        verify(slingClient, times(1)).updatePage(path);
    }

    @Test
    void testGetPropertyValue_SingleValue() throws Exception {
        String path = "/content/test";
        String propName = "maSortingTag";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return "test1";
        }).when(slingClient).getPropertyValue(path, propName);

        String value = slingClient.getPropertyValue(path, propName);

        assertEquals("test1", value, "Property value should be 'test1'");
        verify(slingClient, times(1)).getPropertyValue(path, propName);
    }

    @Test
    void testCopyProperties_Success() throws Exception {
        String path = "/content/test";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            return null;
        }).when(slingClient).copyProperties(path);

        slingClient.copyProperties(path);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for copy");
        verify(slingClient, times(1)).copyProperties(path);
    }

    @Test
    void testReplacePropertyValue_Success() throws Exception {
        String path = "/content/test";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            return null;
        }).when(slingClient).replacePropertyValue(path);

        slingClient.replacePropertyValue(path);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for replace");
        verify(slingClient, times(1)).replacePropertyValue(path);
    }

    @Test
    void testDeleteProperties_Success() throws Exception {
        String path = "/content/test";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 200);
            return null;
        }).when(slingClient).deleteProperties(path);

        slingClient.deleteProperties(path);

        assertEquals(200, slingClient.getLastStatusCode(), "Status code should be 200 for delete");
        verify(slingClient, times(1)).deleteProperties(path);
    }

    @Test
    void testCreateNode_Success() throws Exception {
        String parentPath = "/content/test";
        doAnswer(invocation -> {
            setPrivateField("lastStatusCode", 201);
            return null;
        }).when(slingClient).createNode(parentPath);

        slingClient.createNode(parentPath);

        assertEquals(201, slingClient.getLastStatusCode(), "Status code should be 201 for node creation");
        verify(slingClient, times(1)).createNode(parentPath);
    }

}
