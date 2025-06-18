package co.acu.pagetool.crx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CrxConnectionTest {

    @Test
    void testDefaultConstructor() {
        CrxConnection conn = new CrxConnection();
        assertEquals("admin", conn.getUsername(), "Default username should be admin");
        assertEquals("admin", conn.getPassword(), "Default password should be admin");
        assertEquals("localhost", conn.getHostname(), "Default hostname should be localhost");
        assertEquals("4502", conn.getPort(), "Default port should be 4502");
        assertFalse(conn.isSecure(), "Default secure should be false");
    }

    @Test
    void testConstructorWithUsernamePassword_Valid() {
        CrxConnection conn = new CrxConnection("user1", "pass1");
        assertEquals("user1", conn.getUsername(), "Username should be user1");
        assertEquals("pass1", conn.getPassword(), "Password should be pass1");
        assertEquals("localhost", conn.getHostname(), "Default hostname should be localhost");
        assertEquals("4502", conn.getPort(), "Default port should be 4502");
        assertFalse(conn.isSecure(), "Default secure should be false");
    }

    @Test
    void testConstructorWithUsernamePassword_NullUsername() {
        CrxConnection conn = new CrxConnection(null, "pass1");
        assertEquals("admin", conn.getUsername(), "Null username should default to admin");
        assertEquals("pass1", conn.getPassword(), "Password should be pass1");
        assertEquals("localhost", conn.getHostname(), "Default hostname should be localhost");
        assertEquals("4502", conn.getPort(), "Default port should be 4502");
        assertFalse(conn.isSecure(), "Default secure should be false");
    }

    @Test
    void testConstructorWithUsernamePassword_EmptyPassword() {
        CrxConnection conn = new CrxConnection("user1", "");
        assertEquals("user1", conn.getUsername(), "Username should be user1");
        assertEquals("admin", conn.getPassword(), "Empty password should default to admin");
        assertEquals("localhost", conn.getHostname(), "Default hostname should be localhost");
        assertEquals("4502", conn.getPort(), "Default port should be 4502");
        assertFalse(conn.isSecure(), "Default secure should be false");
    }

    @Test
    void testFullConstructor_Valid() {
        CrxConnection conn = new CrxConnection("user1", "pass1", "host1", "8080", true);
        assertEquals("user1", conn.getUsername(), "Username should be user1");
        assertEquals("pass1", conn.getPassword(), "Password should be pass1");
        assertEquals("host1", conn.getHostname(), "Hostname should be host1");
        assertEquals("8080", conn.getPort(), "Port should be 8080");
        assertTrue(conn.isSecure(), "Secure should be true");
    }

    @Test
    void testFullConstructor_NullValues() {
        CrxConnection conn = new CrxConnection(null, null, null, null, false);
        assertEquals("admin", conn.getUsername(), "Null username should default to admin");
        assertEquals("admin", conn.getPassword(), "Null password should default to admin");
        assertEquals("localhost", conn.getHostname(), "Null hostname should default to localhost");
        assertEquals("4502", conn.getPort(), "Null port should default to 4502");
        assertFalse(conn.isSecure(), "Secure should be false");
    }

    @Test
    void testFullConstructor_EmptyValues() {
        CrxConnection conn = new CrxConnection("", "", "", "", true);
        assertEquals("admin", conn.getUsername(), "Empty username should default to admin");
        assertEquals("admin", conn.getPassword(), "Empty password should default to admin");
        assertEquals("localhost", conn.getHostname(), "Empty hostname should default to localhost");
        assertEquals("4502", conn.getPort(), "Empty port should default to 4502");
        assertTrue(conn.isSecure(), "Secure should be true");
    }

}
