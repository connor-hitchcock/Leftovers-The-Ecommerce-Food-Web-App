package org.seng302.tools;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class provides static methods to generate an authentication code from the user's password and verify that
 * authentication code.
 */
public class PasswordAuthenticator {
    private PasswordAuthenticator() {}

    /**
     * This method generates a hash authentication code for the given password using algorithm SHA-256.
     * @param password Password entered by user upon registration.
     * @return 32 byte hash code generated from given password.
     */
    public static String generateAuthenticationCode(String password) throws NoSuchAlgorithmException {
        MessageDigest passwordHasher = MessageDigest.getInstance("SHA-256");
        byte[] authBytes = passwordHasher.digest(password.getBytes(StandardCharsets.UTF_8));
        return byteArrayToHexString(authBytes);
    }

    /**
     * This method checks the hash authentication code of the provided password against the authenticated stored in the
     * database under the given username.
     * @param storedAuthenticationCode Authentication code saved to database for user.
     * @param password Password entered by user upon login.
     */
    public static void verifyPassword(String password, String storedAuthenticationCode) {
        try {
            String receivedAuthenticationCode = generateAuthenticationCode(password);
            if (!receivedAuthenticationCode.equals(storedAuthenticationCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not verify password");
        }
    }

    protected static String byteArrayToHexString(byte[] bytesToConvert) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte byteToConvert : bytesToConvert) {
            String byteString = Integer.toHexString(Byte.toUnsignedInt(byteToConvert));
            hexStringBuilder.append("00".substring(byteString.length()));
            hexStringBuilder.append(byteString);
        }
        return hexStringBuilder.toString();
    }
}
