package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SearchFormatException extends ResponseStatusException {

    private static final String REASON = "The search query was not formatted correctly.";
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public SearchFormatException() {
        super(HTTP_STATUS, REASON);
    }

    public SearchFormatException(String reason) {
        super(HTTP_STATUS, reason);
    }
}
