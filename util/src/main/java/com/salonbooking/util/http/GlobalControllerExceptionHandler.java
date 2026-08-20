package com.salonbooking.util.http;

import com.salonbooking.util.exceptions.AuthenticationException;
import com.salonbooking.util.exceptions.ConflictException;
import com.salonbooking.util.exceptions.InvalidInputException;
import com.salonbooking.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Centralizovano mapiranje domenskih izuzetaka na HTTP odgovore.
 * Deljeno izmedju svih mikroservisa kroz util modul (isti obrazac kao u knjizi).
 */
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFoundExceptions(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public HttpErrorInfo handleInvalidInputException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public HttpErrorInfo handleConflictException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.CONFLICT, request, ex);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public HttpErrorInfo handleAuthenticationException(WebRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.UNAUTHORIZED, request, ex);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, WebRequest request, Exception ex) {
        String path = request instanceof ServletWebRequest sr
                ? sr.getRequest().getRequestURI()
                : request.getDescription(false);
        String message = ex.getMessage();

        LOG.debug("Vracam HTTP status: {} za putanju: {}, poruka: {}", httpStatus, path, message);
        return new HttpErrorInfo(httpStatus, path, message);
    }
}
