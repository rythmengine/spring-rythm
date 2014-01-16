package org.rythmengine.spring.web.result;

import org.osgl.exception.FastRuntimeException;
import org.osgl.util.E;
import org.rythmengine.spring.web.HttpUtils;
import org.rythmengine.spring.web.RythmExceptionHandler;
import org.rythmengine.utils.S;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by luog on 16/01/14.
 */
public class Result extends FastRuntimeException {

    private HttpStatus status;
    private MessageSource messageSource;

    public Result(HttpStatus status) {
        this.status = status;
        messageSource = RythmExceptionHandler.getMessageSource();
    }

    public Result(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public Result(HttpStatus status, String message, Object... args) {
        super(message, args);
        this.status = status;
    }

    public Result(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public Result(HttpStatus status, Throwable cause, String message, Object... args) {
        super(cause, message, args);
        this.status = status;
    }

    public void apply(HttpServletRequest request, HttpServletResponse response) {
        int statusCode = status.value();
        String reason = getMessage();

        if (this.messageSource != null) {
            reason = this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale());
        }
        if (S.empty(reason)) {
            reason = status.getReasonPhrase();
        }

        String contentType = (null != request) ? HttpUtils.resolveFormat(request).toContentType() : "text/html";
        response.setContentType(contentType);

        try {
            writeToResponse(response, statusCode, reason);
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    protected void writeToResponse(HttpServletResponse response, int statusCode,  String message) throws IOException {
        response.sendError(statusCode, message);
    }

}
