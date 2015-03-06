package org.rythmengine.spring.web.result;

import org.osgl.storage.ISObject;
import org.osgl.storage.impl.SObject;
import org.osgl.util.E;
import org.osgl.util.IO;
import org.osgl.util.S;
import org.rythmengine.spring.web.HttpFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.rythmengine.spring.web.HttpHeaders.CONTENT_DISPOSITION;
import static org.rythmengine.spring.web.HttpHeaders.CONTENT_LENGTH;

/**
 * Created by luog on 22/05/2014.
 */
public class BinaryResult extends Result {
    private static enum Disposition {
        inline, attachment;

        public static Disposition of(boolean inline) {
            return inline ? Disposition.inline : Disposition.attachment;
        }
    }

    private Disposition disposition = Disposition.attachment;
    private long length;
    private String name;
    private ISObject binary;
    private String contentType;


    /**
     * send a binary stream as the response
     * @param is the stream to read from
     * @param name the name to use as Content-Diposition attachement filename
     */
    public BinaryResult(InputStream is, String name) {
        this(is, name, false);
    }

    public BinaryResult(InputStream is, String name, long length) {
        this(is, name, length, false);
    }

    /**
     * send a binary stream as the response
     * @param is the stream to read from
     * @param name the name to use as Content-Diposition attachement filename
     * @param inline true to set the response Content-Disposition to inline
     */
    public BinaryResult(InputStream is, String name, boolean inline) {
        this(is, name, null, inline);
    }

    /**
     * send a binary stream as the response
     * @param is the stream to read from
     * @param name the name to use as Content-Diposition attachement filename
     * @param inline true to set the response Content-Disposition to inline
     */
    public BinaryResult(InputStream is, String name, String contentType, boolean inline) {
        this(is, name, 0, contentType, inline);
    }

    public BinaryResult(InputStream is, String name, long length, boolean inline) {
        this(is, name, length, new MimetypesFileTypeMap().getContentType(name), inline);
    }

    /**
     * Send a file as the response. Content-disposion is set to attachment.
     *
     * @param file readable file to send back
     * @param name a name to use as Content-disposion's filename
     */
    public BinaryResult(File file, String name) {
        this(file, name, false);
        if (file == null) {
            throw new RuntimeException("file is null");
        }
    }

    public BinaryResult(InputStream is, String name, long length, String contentType, boolean inline) {
        super(HttpStatus.OK);
        this.binary = SObject.of(name, is);
        this.name = name;
        this.contentType = contentType;
        this.disposition = Disposition.of(inline);
        this.length = length;
    }

    /**
     * Send a file as the response.
     * Content-disposion is set to attachment, name is taken from file's name
     * @param file readable file to send back
     */
    public BinaryResult(File file) {
        this(file, file.getName(), true);
    }

    /**
     * Send a file as the response.
     * Content-disposion is set to attachment, name is taken from file's name
     * @param file readable file to send back
     */
    public BinaryResult(File file, String name, boolean inline) {
        super(HttpStatus.OK);
        this.binary = SObject.of(name, file);
        this.name = name;
        this.disposition = Disposition.of(inline);
        if (file == null) {
            throw new RuntimeException("file is null");
        }
    }


    @Override
    protected ModelAndView writeToResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        boolean hasName = S.notBlank(name);
        try {
            if (null != contentType) {
                resp.setContentType(contentType);
            } else if (hasName) {
                String ext = S.afterLast(name, ".");
                try {
                    resp.setContentType(HttpFormat.valueOf(ext).toContentType());
                } catch (RuntimeException e) {
                    // ignore
                }
            }
            String disp = disposition.name();
            if (!resp.containsHeader(CONTENT_DISPOSITION)) {
                if (!hasName) {
                    resp.setHeader(CONTENT_DISPOSITION, disp);
                } else {
                    if(canAsciiEncode(name)) {
                        String contentDisposition = "%s; filename=\"%s\"";
                        resp.setHeader(CONTENT_DISPOSITION, S.fmt(contentDisposition, disp, name));
                    } else {
                        final String encoding = resp.getCharacterEncoding();
                        String contentDisposition = "%1$s; filename*="+encoding+"''%2$s; filename=\"%2$s\"";
                        resp.setHeader(CONTENT_DISPOSITION, S.fmt(contentDisposition, disp, URLEncoder.encode(name, encoding)));
                    }
                }
            }
            if (!resp.containsHeader(CONTENT_LENGTH)) {
                if (0 < length) {
                    resp.setHeader(CONTENT_LENGTH, S.string(length));
                }
            }
            IO.copy(binary.asInputStream(), resp.getOutputStream(), false);
        } catch (Exception e) {
            throw E.unexpected(e);
        }

        return new ModelAndView();
    }

    private boolean canAsciiEncode(String string) {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        return asciiEncoder.canEncode(string);
    }
}
