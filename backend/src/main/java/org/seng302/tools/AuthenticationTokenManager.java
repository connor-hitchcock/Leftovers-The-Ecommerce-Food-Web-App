package org.seng302.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.User;
import org.seng302.exceptions.AccessTokenException;
import org.seng302.exceptions.InsufficientPermissionException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * This class provides static methods to generate and verify the authentication tokens used to check whether a user is
 * logged in.
 */
public class AuthenticationTokenManager {

    private AuthenticationTokenManager() { }

    private static final String AUTH_TOKEN_NAME = "AUTHTOKEN";
    private static final Logger logger = LogManager.getLogger(AuthenticationTokenManager.class.getName());
    private static final String ROLE_DGAA = "defaultGlobalApplicationAdmin";
    private static final String ROLE_GAA = "globalApplicationAdmin";

    /**
     * This method sets the authentication token for this session and constructs a cookie containing the authentication
     * token which will be attached to the HTTP response.
     * @param request The HTTP request packet.
     * @param response The HTTP response packet.
     * @param user the user associated with the login
     */
    public static String setAuthenticationToken(HttpServletRequest request, HttpServletResponse response, User user) {
        String authString = generateAuthenticationString();

        Cookie authToken = new Cookie(AUTH_TOKEN_NAME, authString);
        authToken.setPath("/");
        authToken.setHttpOnly(true);
        authToken.setMaxAge(30 * 60);   // Set cookie expiry for 30 minutes from now
        response.addCookie(authToken);

        HttpSession session = request.getSession(true);
        session.setAttribute(AUTH_TOKEN_NAME, authString);
        // Tag session with account ID (if it isnt null (dgaa)
        if (user != null) {
            session.setAttribute("accountId", user.getUserID());
            session.setAttribute("role", user.getRole());
        }
        // Sends auth token to be sent in the response body
        return "{\"authToken\": \"" + authString + "\"}";
    }

    /**
     * Overload
     * @param request The HTTP request packet.
     * @param response The HTTP response packet.
     */
    public static String setAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
        return setAuthenticationToken(request, response, null);
    }

    /**
     * This method generates a random 16-byte code an converts it to a hexidecimal string which can be used as the
     * authentication code for the session.
     * @return A 32-character string representing 16 random bytes in hexidecimal notation.
     */
    private static String generateAuthenticationString() {
        SecureRandom random = new SecureRandom();
        byte[] authBytes = new byte[16];
        random.nextBytes(authBytes);
        return PasswordAuthenticator.byteArrayToHexString(authBytes);
    }

    /**
     * This method checks that the authentication token for the HTTP request matches the expected token for this session.
     * @param request The HTTP request packet.
     */
    public static void checkAuthenticationToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String expectedAuthString = (String) session.getAttribute(AUTH_TOKEN_NAME);
        if (expectedAuthString == null) {
            AccessTokenException accessTokenException = new AccessTokenException("Access token not present for session.");
            logger.error(accessTokenException.getMessage());
            throw accessTokenException;
        }
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies != null) {
            for (Cookie cookie : requestCookies) {
                if (cookie.getName().equals(AUTH_TOKEN_NAME)) {
                    if (cookie.getValue().equals(session.getAttribute(AUTH_TOKEN_NAME))) {
                        return;
                    } else {
                        AccessTokenException accessTokenException = new AccessTokenException("Invalid access token.");
                        logger.error(accessTokenException.getMessage());
                        throw accessTokenException;
                    }
                }
            }
        }
        AccessTokenException accessTokenException = new AccessTokenException("Access token not present in request.");
        logger.error(accessTokenException.getMessage());
        throw accessTokenException;
    }

    /**
     * This method tags the current session as a DGAA session
     * @param request The HTTP request packet
     */
    public static void setAuthenticationTokenDGAA(HttpServletRequest request) {

        HttpSession session = request.getSession(true);
        session.setAttribute("role", ROLE_DGAA);
    }

    /**
     * This method checks to see if the current session in a DGAA session
     * @param request The HTTP request packet
     */
    public static void checkAuthenticationTokenDGAA(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionRole = (String)session.getAttribute("role");
        if (!ROLE_DGAA.equals(sessionRole)) {
            InsufficientPermissionException insufficientPermissionException = new InsufficientPermissionException("The user does not have permission to perform the requested action");
            logger.error(insufficientPermissionException.getMessage());
            throw insufficientPermissionException;
        }
    }

    /**
     * Given a HTTP request, and a given account ID, this method determines if the currently logged in account can see the private info of the given ID
     * When an account has role "globalApplicationAdmin" or "defaultGlobalApplicationAdmin" then permission is granted
     * @param request The HTTP request packet
     * @param accountId The account Id to compare
     * @return True if accountId matches session
     */
    public static boolean sessionCanSeePrivate(HttpServletRequest request, Long accountId) {
        HttpSession session = request.getSession();
        Long sessionAccountId = (Long)session.getAttribute("accountId");
        if (Objects.equals(sessionAccountId, accountId)) {
            return true;
        } else return sessionIsAdmin(request);
    }

    /**
     * This method returns true if the account associated with the HTTP request is a default global application admin
     * or global application admin, and false if the account has any other role. This method should only be called after
     * the AUTH-TOKEN associated with the request has been verified with the CheckAuthenticationToken method.
     * @param request A HTTP request packet with a verified authentication token.
     * @return True if the account is an admin, false otherwise.
     */
    public static boolean sessionIsAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionRole = (String) session.getAttribute("role");
        return (ROLE_GAA.equals(sessionRole) || ROLE_DGAA.equals(sessionRole));
    }
}
