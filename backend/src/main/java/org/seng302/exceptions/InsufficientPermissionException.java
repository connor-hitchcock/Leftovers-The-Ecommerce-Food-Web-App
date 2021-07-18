package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request have sufficient permission to request this operation
 */

public class InsufficientPermissionException extends ResponseStatusException {

    private static final HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;
    private static final String REASON = "Invalid access token.";

    public InsufficientPermissionException() {
        super(HTTP_STATUS, REASON);
    }

    public InsufficientPermissionException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
