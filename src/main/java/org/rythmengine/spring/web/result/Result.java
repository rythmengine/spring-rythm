package org.rythmengine.spring.web.result;

/*-
 * #%L
 * Spring Rythm Plugin
 * %%
 * Copyright (C) 2017 - 2018 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgl.exception.FastRuntimeException;
import org.osgl.util.E;
import org.osgl.util.S;
import org.rythmengine.spring.web.HttpUtils;
import org.rythmengine.spring.web.RythmExceptionHandler;
import org.rythmengine.spring.web.util.Interceptors;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class Result extends FastRuntimeException {

    private static int[] errorCodes = {
        403, 404, 500
    };

    public static boolean isError(int status) {
        return Arrays.binarySearch(errorCodes, status) != -1;
    }

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

    public ModelAndView apply(HttpServletRequest request, HttpServletResponse response) {
        int statusCode = status.value();
        String reason = getMessage();

        if (this.messageSource != null) {
            reason = this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale());
        }
        if (S.blank(reason)) {
            reason = status.getReasonPhrase();
        }

        Interceptors.applyPostHandlers(request, response);

        String contentType = (null != request) ? HttpUtils.resolveFormat(request).toContentType() : "text/html";
        response.setContentType(contentType);

        try {
            return writeToResponse(response, statusCode, reason);
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    protected ModelAndView writeToResponse(HttpServletResponse response, int statusCode,  String message) throws IOException {
        ModelAndView mv = new ModelAndView();
        if (isError(statusCode)) {
            mv.setViewName("errors/prod/error.html");
            mv.addObject("statusCode", statusCode);
            mv.addObject("message", message);
        }
        response.sendError(statusCode, message);
        return new ModelAndView();
    }

}
