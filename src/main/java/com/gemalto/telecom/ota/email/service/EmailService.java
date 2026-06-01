package com.gemalto.telecom.ota.email.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemalto.telecom.ota.email.api.model.EmailForm;
import com.gemalto.telecom.ota.email.api.model.PendingEmailRequest;
import com.gemalto.telecom.ota.email.util.TemplateProcessor;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    private static final String DEFAULT_SUBJECT_PATH    = "/templates/default-subject.txt";
    private static final String DEFAULT_BODY_PATH       = "/templates/default-body.html";
    private static final String DEFAULT_ATTACHMENT_PATH = "/attachments/Rajat_Kumar_Singh.pdf";
    private static final String DEFAULT_ATTACHMENT_NAME = "Rajat_Kumar_Singh.pdf";

    @Inject Mailer mailer;
    @Inject TemplateProcessor templateProcessor;
    @Inject PendingEmailStore pendingStore;

    @ConfigProperty(name = "quarkus.mailer.username", defaultValue = "")
    String mailerUsername;

    @ConfigProperty(name = "quarkus.mailer.password", defaultValue = "")
    String mailerPassword;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String defaultSubject;
    private String defaultBody;
    private byte[] defaultAttachmentBytes;

    @PostConstruct
    void loadDefaults() {
        defaultSubject        = loadText(DEFAULT_SUBJECT_PATH,  "Email Notification");
        defaultBody           = loadText(DEFAULT_BODY_PATH,     "<p>Hello,</p><p>This is a notification.</p>");
        defaultAttachmentBytes = loadBytes(DEFAULT_ATTACHMENT_PATH);
    }

    public boolean isSmtpConfigured() {
        return mailerUsername != null && !mailerUsername.isBlank()
            && mailerPassword != null && !mailerPassword.isBlank();
    }

    /**
     * Sends an email or, if SMTP is not configured, queues it in ./pending-emails/.
     * @return true if sent immediately, false if queued
     */
    public boolean send(EmailForm form) {
        Map<String, String> replacements = parseReplacements(form.replacements);
        System.out.println(replacements);
        String subject = resolve(form.subject, defaultSubject, replacements);
        String body    = resolve(form.body,    defaultBody,    replacements);

        byte[] attachBytes = (form.attachment != null && form.attachment.length > 0)
                ? form.attachment : defaultAttachmentBytes;
        String attachName  = (form.attachmentName != null && !form.attachmentName.isBlank())
                ? form.attachmentName : DEFAULT_ATTACHMENT_NAME;

        if (!isSmtpConfigured()) {
            LOG.warn("SMTP not configured — queuing email instead of sending");
            pendingStore.save(PendingEmailRequest.of(
                null, form.to, subject, body, attachBytes, attachName, replacements
            ));
            return false;
        }
        LOG.infof("form.attachment length=%s",
                form.attachment == null ? "null" : form.attachment.length);

        LOG.infof("defaultAttachmentBytes length=%d",
                defaultAttachmentBytes.length);

        doSend(form.to, subject, body, attachBytes, attachName);
        return true;
    }

    /**
     * Flushes all queued emails. Requires SMTP to be configured.
     * @return number of emails sent
     */
    public int flushPending() {
        if (!isSmtpConfigured()) {
            throw new IllegalStateException("Cannot flush pending emails: SMTP not configured");
        }
        List<PendingEmailRequest> all = pendingStore.loadAll();
        int sent = 0;
        for (PendingEmailRequest req : all) {
            try {
                byte[] attach = req.attachmentBytes();
                doSend(req.to, req.subject, req.body, attach, req.attachmentName);
                pendingStore.delete(req.id);
                sent++;
            } catch (Exception e) {
                LOG.errorf(e, "Failed to send pending email %s to %s", req.id, req.to);
            }
        }
        return sent;
    }

    public int pendingCount() {
        return pendingStore.count();
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private void doSend(String to, String subject, String body,
                        byte[] attachBytes, String attachName) {

        Mail mail = Mail.withHtml(to, subject, body);

        if (attachBytes != null && attachBytes.length > 0) {

            LOG.infof("Attachment name=%s", attachName);
            LOG.infof("PDF size=%d bytes", attachBytes.length);
            LOG.infof("PDF header=%s", new String(attachBytes, 0, 5));

            mail.addAttachment(
                    attachName,
                    attachBytes,
                    "application/pdf"
            );
        }

        mailer.send(mail);
    }

    private String resolve(String userValue, String defaultValue, Map<String, String> replacements) {
        String template = (userValue != null && !userValue.isBlank()) ? userValue : defaultValue;
        return templateProcessor.process(template, replacements);
    }

    private Map<String, String> parseReplacements(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            LOG.warnf("Could not parse replacements JSON: %s", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private String loadText(String resourcePath, String fallback) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) { LOG.warnf("Resource not found: %s", resourcePath); return fallback; }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            LOG.warnf("Failed to load %s: %s", resourcePath, e.getMessage());
            return fallback;
        }
    }

    private byte[] loadBytes(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) { LOG.warnf("Resource not found: %s", resourcePath); return new byte[0]; }
            return is.readAllBytes();
        } catch (IOException e) {
            LOG.warnf("Failed to load %s: %s", resourcePath, e.getMessage());
            return new byte[0];
        }
    }
}
