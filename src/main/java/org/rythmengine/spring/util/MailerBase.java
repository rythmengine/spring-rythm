package org.rythmengine.spring.util;

import org.osgl._;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;
import org.rythmengine.RythmEngine;
import org.rythmengine.spring.web.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by luog on 23/01/14.
 */
public class MailerBase implements InitializingBean {

    static Logger logger = L.get(MailerBase.class);

    private static RythmEngine engine;

    private static boolean underscoreImplicitVarNames;
    private static boolean enableSessionManager;
    private static boolean enableUserAgentDetector;
    private static JavaMailSenderImpl mailer;
    protected static final ThreadLocal<MailInfo> info = new ThreadLocal<MailInfo>() {
        @Override
        protected MailInfo initialValue() {
            return new MailInfo();
        }
    };


    private static RythmEngine engine() {
        if (null == engine) {
            engine = RythmConfigurer.engine();
            if (null == engine) return null; //wait for next init
            Object o = engine.getProperty(RythmConfigurer.CONF_UNDERSCORE_IMPLICIT_VAR_NAME);
            if (null != o) {
                try {
                    underscoreImplicitVarNames = (Boolean) o;
                } catch (Exception e) {
                    // ignore it
                    logger.warn(e, "error set underscore implicit variable name config");
                }
            }
            o = engine.getProperty(RythmConfigurer.CONF_ENABLE_SESSION_MANAGER);
            if (null != o) {
                try {
                    enableSessionManager = (Boolean) o;
                } catch (Exception e) {
                    // ignore it
                    logger.warn(e, "error set enable session manager config");
                }
            }
            o = engine.getProperty(RythmConfigurer.CONF_ENABLE_USER_AGENT_DETECTOR);
            if (null != o) {
                try {
                    enableUserAgentDetector = (Boolean)o;
                } catch (Exception e) {
                    logger.warn(e, "error set enable user agent detector config");
                }
            }
        }
        return engine;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        engine();
    }

    public static class MailInfo {
        String from = "noreply@osgl.org";
        C.List<String> recipients = C.newList();
        C.List<String> ccs = C.newList();
        C.List<String> bccs = C.newList();
        String replyTo = null;
        String subject;
        String charset = "utf-8";
        String contentType = "text/html";
        String html;
        String text;
        C.Map<String, Object> renderArgs = C.newMap();

        public void send() {
            try {
                String[] sa = {};
                MimeMessage message = mailer.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, charset);
                if (!recipients.isEmpty()) helper.setTo(recipients.toArray(sa));
                if (!ccs.isEmpty()) helper.setCc(ccs.toArray(sa));
                if (!bccs.isEmpty()) helper.setBcc(bccs.toArray(sa));
                if (S.notBlank(from)) helper.setFrom(from);
                if (S.notBlank(replyTo)) helper.setReplyTo(replyTo);

                helper.setSubject(subject);
                boolean u = underscoreImplicitVarNames;
                HttpServletRequest request = SessionManager.request();
                if (null != request) {
                    // so this is running from a http request handling thread
                    C.Map<String, Object> params = renderArgs;
                    params.put(u ? "_request" : "request", request);
                    params.put("__request", request);
                    params.put(u ? "_response" : "response", SessionManager.response());
                    HttpSession httpSession = request.getSession(false);
                    params.put(u ? "_httpSession" : "httpSession", httpSession);
                    if (enableSessionManager) {
                        params.put(u ? "_session" : "session", Session.current());
                        params.put(u ? "_flash" : "flash", Flash.current());
                    }
                    if (enableUserAgentDetector) {
                        params.put(u ? "_userAgent" : "userAgent", UADetector.get());
                    }
                }

                if (S.notBlank(html)) {
                    html = engine().render(html, renderArgs);
                    helper.setText(html, true);
                }
                if (S.notBlank(text)) {
                    text = engine.render(text, renderArgs);
                    helper.setText(text, false);
                }
                mailer.send(message);
            } catch (Exception e) {
                logger.error(e, "Error sending email");
            } finally {
                info.remove();
            }
        }
    }

    private static boolean isEmail(String s) {
        if (S.blank(s)) return false;
        return s.toLowerCase().matches("^[_a-z0-9-']+(\\.[_a-z0-9-']+)*(\\+[0-9]+)?@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$");
    }

    protected static MailInfo setFrom(String from) {
        E.illegalArgumentIf(!isEmail(from));
        MailInfo mi = info.get();
        mi.from = from;
        mi.replyTo = from;
        return mi;
    }

    protected static MailInfo setContentType(String contentType) {
        MailInfo mi = info.get();
        if (S.blank(contentType)) return mi;
        mi.contentType = contentType;
        return mi;
    }

    protected static MailInfo setCharSet(String charset) {
        MailInfo mi = info.get();
        if (S.blank(charset)) return mi;
        mi.charset = charset;
        return mi;
    }

    protected static MailInfo setSubject(String subject) {
        MailInfo mi = info.get();
        if (S.blank(subject)) return mi;
        mi.subject = subject;
        return mi;
    }

    protected static MailInfo addRecipients(String... mail) {
        MailInfo mi = info.get();
        if (mail.length == 0) return mi;
        mi.recipients.addAll(C.listOf(mail));
        return mi;
    }

    protected static MailInfo addRecipients(List<String> mails) {
        MailInfo mi = info.get();
        if (mails.size() == 0) return mi;
        mi.recipients.addAll(mails);
        return mi;
    }

    protected static MailInfo addCc(String... mail) {
        MailInfo mi = info.get();
        if (mail.length == 0) return mi;
        mi.ccs.addAll(C.listOf(mail));
        return mi;
    }

    protected static MailInfo addCc(List<String> mails) {
        MailInfo mi = info.get();
        if (mails.size() == 0) return mi;
        mi.ccs.addAll(mails);
        return mi;
    }

    protected static MailInfo addBcc(String... mail) {
        MailInfo mi = info.get();
        if (mail.length == 0) return mi;
        mi.bccs.addAll(C.listOf(mail));
        return mi;
    }

    protected static MailInfo addBcc(List<String> mails) {
        MailInfo mi = info.get();
        if (mails.size() == 0) return mi;
        mi.bccs.addAll(mails);
        return mi;
    }

    protected static MailInfo setText(String text) {
        MailInfo mi = info.get();
        if (S.blank(text)) return mi;
        mi.text = text;
        return mi;
    }

    protected static MailInfo setHtml(String html) {
        MailInfo mi = info.get();
        if (S.blank(html)) return mi;
        mi.html = html;
        return mi;
    }

    protected static MailInfo setRenderArg(String key, Object val) {
        MailInfo mi = info.get();
        if (null != val) mi.renderArgs.put(key, val);
        return mi;
    }

    protected static MailInfo setRenderArgs(_.T2<String, ?> arg1, _.T2<String, ?>... args) {
        MailInfo mi = setRenderArg(arg1._1, arg1._2);
        for (_.T2<String, ?> arg : args) {
            setRenderArg(arg._1, arg._2);
        }
        return mi;
    }

    protected static void begin() {
        info.remove();
    }

    protected static void send() {
        info.get().send();
    }

    public void setMailer(JavaMailSenderImpl mailSender) {
        mailer = mailSender;
    }

    public static void main(String[] args) {
        String s = "allen.x@wipro.com";
        System.out.println(isEmail(s));
    }

}
