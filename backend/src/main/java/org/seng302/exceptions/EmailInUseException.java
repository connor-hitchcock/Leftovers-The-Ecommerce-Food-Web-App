package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request is made to register an account with an email that is already in use.
 */
public class EmailInUseException extends ResponseStatusException {

    private static final String REASON = "There is already an account associated with that email.";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public EmailInUseException() {
        super(HTTP_STATUS, REASON);
    }

    public EmailInUseException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
