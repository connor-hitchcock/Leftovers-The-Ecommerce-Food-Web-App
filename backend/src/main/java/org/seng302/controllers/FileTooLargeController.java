package org.seng302.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class FileTooLargeController {
    private static final Logger logger = LogManager.getLogger(FileTooLargeController.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleFileSizeLimitExceeded(MaxUploadSizeExceededException exc, HttpServletRequest request) {
        logger.info("Attempted to upload file that was too large");
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                // Since this middleware gets executed before the cors middleware then we have to do our own cors
                .header("Access-Control-Allow-Origin", request.getHeader("origin"))
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body("Uploaded file too large");
    }
}
