package com.gemalto.telecom.ota.email.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/** JSON-serialisable snapshot of an email that could not be sent (no SMTP config). */
public class PendingEmailRequest {

    @JsonProperty("id")
    public String id;

    @JsonProperty("queuedAt")
    public String queuedAt;

    @JsonProperty("to")
    public String to;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("body")
    public String body;

    /** Base-64 encoded attachment bytes. Null means "use repo default". */
    @JsonProperty("attachmentBase64")
    public String attachmentBase64;

    @JsonProperty("attachmentName")
    public String attachmentName;

    @JsonProperty("replacements")
    public Map<String, String> replacements;

    public PendingEmailRequest() {}

    public static PendingEmailRequest of(String id,
                                         String to,
                                         String subject,
                                         String body,
                                         byte[] attachmentBytes,
                                         String attachmentName,
                                         Map<String, String> replacements) {
        PendingEmailRequest r = new PendingEmailRequest();
        r.id              = id;
        r.queuedAt        = Instant.now().toString();
        r.to              = to;
        r.subject         = subject;
        r.body            = body;
        r.attachmentBase64 = (attachmentBytes != null && attachmentBytes.length > 0)
                            ? Base64.getEncoder().encodeToString(attachmentBytes)
                            : null;
        r.attachmentName  = attachmentName;
        r.replacements    = replacements;
        return r;
    }

    public byte[] attachmentBytes() {
        if (attachmentBase64 == null || attachmentBase64.isBlank()) return null;
        return Base64.getDecoder().decode(attachmentBase64);
    }
}
