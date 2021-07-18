package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request does not include an access token or when the access token cannot be
 * authenticated.
 */

public class AccessTokenException extends ResponseStatusException {

    private static final HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;
    private static final String REASON = "Invalid access token.";

    public AccessTokenException() {
        super(HTTP_STATUS, REASON);
    }

    public AccessTokenException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
