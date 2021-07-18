package org.seng302.tools;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.seng302.exceptions.AccessTokenException;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationTokenManagerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Captor
    ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @Captor
    ArgumentCaptor<String> nameArgumentCaptor;

    @Captor
    ArgumentCaptor<String> valueArgumentCaptor;

    private final String authTokenName = "AUTHTOKEN";

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verify that when setAuthenticationToken is called a cookie with name "AUTHTOKEN" is added to the HTTP response.
     */
    @Test
    void setAuthenticationTokenCookieAddedTest() {
        when(request.getSession(true)).thenAnswer(
                invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertEquals(authTokenName, responseCookie.getName());
    }

    /**
     * Verify that when setAuthenticationToken is called the AUTHTOKEN cookie contains a 32 character hexidemcimal string.
     */
    @Test
    void setAuthenticationTokenCookieValueTest() {
        when(request.getSession(true)).thenAnswer(
                invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertTrue(responseCookie.getValue().matches("[abcdef\\d]{32}"));
    }

    /**
     * Verify that when setAuthenticationToken is called the AUTHTOKEN cookie is set to expire in 30 minutes.
     */
    @Test
    void setAuthenticationTokenCookieExpiryTest() {
        when(request.getSession(true)).thenAnswer(
                invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertEquals(30 * 60, responseCookie.getMaxAge());
    }

    /**
     * Verify that when setAuthenticationToken is called an attribute with name AUTHTOKEN is added to the session.
     */
    @Test
    void setAuthenticationTokenSessionAttributeAddedTest() {
        when(request.getSession(true)).thenAnswer(
                invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        Mockito.verify(session).setAttribute(nameArgumentCaptor.capture(), valueArgumentCaptor.capture());
        String attributeName = nameArgumentCaptor.getValue();
        assertEquals(authTokenName, attributeName);
    }

    /**
     * Verify that when setAuthenticationToken is called the value of the cookie AUTHTOKEN added to the response is the
     * same as the value of the attribute AUTHTOKEN added to the session.
     */
    @Test
    void setAuthenticationTokenSessionAttributeCookieMatchTest() {
        when(request.getSession(true)).thenAnswer(
                invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        Mockito.verify(session).setAttribute(nameArgumentCaptor.capture(), valueArgumentCaptor.capture());
        String attributeValue = valueArgumentCaptor.getValue();
        assertEquals(responseCookie.getValue(), attributeValue);
    }

    /**
     * Verify that when checkAuthenticationToken is called and the cookie AUTHTOKEN is not present in the request an
     * AccessTokenException is thrown.
     */
    @Test
    void checkAuthenticationTokenCookieNotPresentTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> new Cookie[0]);
        assertThrows(AccessTokenException.class, () ->
            AuthenticationTokenManager.checkAuthenticationToken(request)
        );
    }

    /**
     * Verify that when checkAuthenticationToken is called and the attribute AUTHTOKEN does not exist for this session
     * an AccessTokenException is thrown.
     */
    @Test
    void checkAuthenticationTokenSessionAttributeNotPresentTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                invocation -> null);
        when(request.getCookies()).thenAnswer(
                invocation -> new Cookie[0]);
        assertThrows(AccessTokenException.class, () ->
            AuthenticationTokenManager.checkAuthenticationToken(request)
        );
    }

    /**
     * Verify that when checkAuthenticationToken is called and the value of the cookie AUTHTOKEN does not match the
     * value of the attribute AUTHTOKEN for this session an AccessTokenException is thrown.
     */
    @Test
    void checkAuthenticationTokenNoMatchTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie(authTokenName, "1234abcd");
                    return cookieArray;
                });
        assertThrows(AccessTokenException.class, () ->
            AuthenticationTokenManager.checkAuthenticationToken(request)
        );
    }

    /**
     * Verify that when checkAuthenticationToken is called and the value of the cookie AUTHTOKEN matches the
     * value of the attribute AUTHTOKEN for this session an no exception is thrown.
     */
    @Test
    void checkAuthenticationTokenMatchTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie(authTokenName, "abcd1234");
                    return cookieArray;
                });
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            assertTrue(true);
        } catch (RuntimeException e) {
            fail("Exception should not be thrown when stored authentication token matches authentication token in cookie");
        }
    }

    /**
     * Verify that sessionIsAdmin returns false when the role associated with the the session is null, 'user' or another
     * string, and that it returns true when the role is 'globalApplicationAdmin' or 'defaultGlobalApplicationAdmin'.
     */
    @ParameterizedTest
    @CsvSource({",false", "user,false", "potato,false", "globalApplicationAdmin,true", "defaultGlobalApplicationAdmin,true"})
    void sessionIsAdminTest(String testRole, boolean expectedValue) {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("role")).thenReturn(testRole);
        assertEquals(expectedValue, AuthenticationTokenManager.sessionIsAdmin(request));
    }

}