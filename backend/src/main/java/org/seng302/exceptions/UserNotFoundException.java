package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a get request is made for a user id which is not present in the application's
 * database.
 */
public class UserNotFoundException extends ResponseStatusException {

    private static final String REASON = "User not found.";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_ACCEPTABLE;

    public UserNotFoundException() {
        super(HTTP_STATUS, REASON);
    }

    public UserNotFoundException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
