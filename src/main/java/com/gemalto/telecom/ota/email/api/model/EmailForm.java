package com.gemalto.telecom.ota.email.api.model;

import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import jakarta.ws.rs.core.MediaType;

/**
 * Multipart form fields for the send-email endpoint.
 * All fields except {@code to} are optional; defaults are loaded from resources.
 */
public class EmailForm {

    /** Recipient email address. Required. */
    @FormParam("to")
    @PartType(MediaType.TEXT_PLAIN)
    public String to;

    /** Email subject/title. Optional — falls back to default-subject.txt. */
    @FormParam("subject")
    @PartType(MediaType.TEXT_PLAIN)
    public String subject;

    /** Email body (HTML allowed). Optional — falls back to default-body.html. */
    @FormParam("body")
    @PartType(MediaType.TEXT_PLAIN)
    public String body;

    /** File attachment bytes. Optional — falls back to default-attachment.txt. */
    @FormParam("attachment")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] attachment;

    /** Original filename for the attachment (used when attachment bytes are provided). */
    @FormParam("attachmentName")
    @PartType(MediaType.TEXT_PLAIN)
    public String attachmentName;

    /**
     * JSON map of placeholder replacements, e.g. {"name":"Alice","date":"June 5"}.
     * Replaces {{key}} tokens in subject and body templates.
     * If absent or empty, placeholders are left unchanged.
     */
    @FormParam("replacements")
    @PartType(MediaType.TEXT_PLAIN)
    public String replacements;
}
