package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a get request is made for a user id which is not present in the application's
 * database.
 */
public class BusinessNotFoundException extends ResponseStatusException {

    private static final String REASON = "Business not found.";
    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_ACCEPTABLE;

    public BusinessNotFoundException() {
        super(HTTP_STATUS, REASON);
    }

    public BusinessNotFoundException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
