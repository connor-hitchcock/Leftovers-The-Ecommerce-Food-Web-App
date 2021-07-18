package cucumber;

import io.cucumber.java.Before;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private Cookie authCookie;
    private final Map<String, Object> sessionAuthToken = new HashMap<>();
    private long loggedInId;

    @Before
    public void setup() {
        authCookie = null;
        sessionAuthToken.clear();
    }

    /**
     * Sets the currently logged in user id
     * @param id User id to log in as
     */
    public void setLoggedInAccount(long id) {
        String authCode = "0".repeat(64);
        authCookie = new Cookie("AUTHTOKEN", authCode);
        sessionAuthToken.put("AUTHTOKEN", authCode);
        sessionAuthToken.put("accountId", id);
        loggedInId = id;
    }

    /**
     * Adds authentication to the provided request builder, if already set
     * @param builder Builder to add authentication to
     * @return Modified builder with authentication
     */
    public MockHttpServletRequestBuilder addAuthorisationToken(MockHttpServletRequestBuilder builder) {
        if (authCookie == null) {
            return builder; // If no authorisation then do nothing
        }

        return builder
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie);
    }

    /**
     * Get the ID number of the user who is currently authenticated in this class's authentication token and request
     * cookie.
     * @return The ID number of the authenticated user
     */
    public long getLoggedInId() {
        return loggedInId;
    }
}
