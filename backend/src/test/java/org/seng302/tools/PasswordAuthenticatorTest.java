package org.seng302.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordAuthenticatorTest {

    /**
     * Test that generateAuthenticationCode returns the expected code for a password less than 32 bytes long.
     */
    @Test
    void generateAuthenticationCodeShortPasswordTest() {
        String expectedCode = PasswordAuthenticator.byteArrayToHexString(new byte[] {-97, -122, -48, -127, -120, 76, 125, 101, -102, 47, -22, -96, -59, 90, -48, 21, -93, -65,
                79, 27, 43, 11, -126, 44, -47, 93, 108, 21, -80, -16, 10, 8});
        try {
            String actualCode = PasswordAuthenticator.generateAuthenticationCode("test");
            assertEquals(expectedCode, actualCode);
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    /**
     * Test that generateAuthenticationCode returns the expected code for a password more than 32 bytes long.
     */
    @Test
    void generateAuthenticationCodeLongPasswordTest() {
        StringBuilder testPassword = new StringBuilder();
        testPassword.append("thisisaverylongpassword".repeat(100));
        String expectedCode = PasswordAuthenticator.byteArrayToHexString(new byte[] {16, 16, -13, 81, 44, -127, 39, 73, -76, 104, 127, -16, -43, 114, 7, -110, -46, 71, -59,
                75, 2, 63, 8, 89, 95, 65, -112, -53, 69, -83, -73, -62});
        try {
            String actualCode = PasswordAuthenticator.generateAuthenticationCode(testPassword.toString());
            assertEquals(expectedCode, actualCode);
        } catch (NoSuchAlgorithmException e) {
            fail();
        }
    }

    /**
     * Test that verifyPassword does not throw an exception when authentication code of given password matches
     * stored authentication code.
     */
    @Test
    void verifyPasswordShouldMatchTest() {
        try {
            String authenticationCode = PasswordAuthenticator.generateAuthenticationCode("password123");
            PasswordAuthenticator.verifyPassword("password123", authenticationCode);
        } catch (Exception e) {
            fail("Exception should not be thrown for matching passwords");
        }
    }

    /**
     * Test that verifyPassword throws a FailedLoginException when authentication code of given password
     * does not match stored authentication code.
     */
    @Test
    void verifyPasswordShouldNotMatchTest() {
        try {
            String authenticationCode = PasswordAuthenticator.generateAuthenticationCode("password123");
            assertThrows(ResponseStatusException.class, () -> {
                PasswordAuthenticator.verifyPassword("passwerd123", authenticationCode);
            });
        } catch (NoSuchAlgorithmException e) {
            fail("SHA-256 algorithm should be used.");
        }
    }

    /**
     * Test that byteArrayToHexString generates the expected hexidecimal string when an empty byte array is given.
     */
    @Test
    void byteArrayToHexStringEmptyArrayTest() {
        byte[] testBytes = new byte[0];
        String result = PasswordAuthenticator.byteArrayToHexString(testBytes);
        assertEquals("", result);
    }

    /**
     * Test that byteArray to hex string generates the expected hexidecimal string from a 16 byte array.
     */
    @Test
    void byteArrayToHexString32ByteTest() {
        byte[] testBytes = new byte[] {-126, 53, 122, 107, 11, 66, -105, 12, 33, -31, 107, 71, -124, 101, 88, -42};
        String expectedString = "82357a6b0b42970c21e16b47846558d6";
        assertEquals(expectedString, PasswordAuthenticator.byteArrayToHexString(testBytes));
    }


}