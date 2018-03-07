package org.rythmengine.spring.web;

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

import org.osgl.util.S;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by luog on 30/12/13.
 */
public enum HttpFormat {
    html {
        @Override
        public String toContentType() {
            return "text/html";
        }
    },
    xml {
        @Override
        public String toContentType() {
            return "text/xml";
        }
    },
    json {
        @Override
        public String toContentType() {
            return "application/json";
        }

        @Override
        public String errorMessage(String message) {
            return S.fmt("{\"error\": \"%s\"}", message);
        }
    },
    xls {
        @Override
        public String toContentType() {
            return "application/vnd.ms-excel";
        }
    },
    xlsx {
        @Override
        public String toContentType() {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
    },
    doc {
        @Override
        public String toContentType() {
            return "application/vnd.ms-word";
        }
    },
    docx {
        @Override
        public String toContentType() {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
    },
    csv {
        @Override
        public String toContentType() {
            return "text/csv";
        }
    },
    txt {
        @Override
        public String toContentType() {
            return "text/plain";
        }
    };

    public static final String ATTR_FORMAT = "__fmt";

    public static HttpFormat resolve(HttpServletRequest request) {
        HttpFormat fmt = (HttpFormat) request.getAttribute(ATTR_FORMAT);
        if (null != fmt) return fmt;

        String accept = request.getHeader(HttpHeaders.ACCEPT);
        fmt = HttpFormat.html; // default

        if (null == accept) {
            fmt = html;
        } else if (accept.contains("application/xhtml") || accept.contains("text/html") || accept.startsWith("*/*")) {
            fmt = html;
        } else if (accept.contains("application/xml") || accept.contains("text/xml")) {
            fmt = xml;
        } else if (accept.contains("application/json") || accept.contains("text/javascript")) {
            fmt = json;
        } else if (accept.contains("text/plain")) {
            fmt = txt;
        } else if (accept.contains("csv") || accept.contains("comma-separated-values")) {
            fmt = csv;
        } else if (accept.contains("ms-excel")) {
            fmt = xls;
        } else if (accept.contains("spreadsheetml")) {
            fmt = xlsx;
        } else if (accept.contains("msword")) {
            fmt = doc;
        } else if (accept.contains("wordprocessingml")) {
            fmt = docx;
        }

        request.setAttribute(ATTR_FORMAT, fmt);
        return fmt;
    }

    public abstract String toContentType();

    public String errorMessage(String message) {
        return message;
    }
}
