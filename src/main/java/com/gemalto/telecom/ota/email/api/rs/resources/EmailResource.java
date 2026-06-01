package com.gemalto.telecom.ota.email.api.rs.resources;

import com.gemalto.telecom.ota.email.api.model.EmailForm;
import com.gemalto.telecom.ota.email.api.model.EmailResponse;
import com.gemalto.telecom.ota.email.service.EmailService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Path("/api/email")
@RequestScoped
@Tag(name = "Email", description = "Send emails with optional defaults loaded from the repository")
public class EmailResource {

    private static final Logger LOG = Logger.getLogger(EmailResource.class);

    @Inject
    EmailService emailService;

    // ── Send ─────────────────────────────────────────────────────────────────

    @POST
    @Path("/send")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Send an email",
        description = "Sends an email to the specified recipient using templates from the repository. "
                    + "If SMTP credentials are not configured the request is saved to ./pending-emails/ "
                    + "and can be flushed later via POST /api/email/send-pending."
    )
    @APIResponse(responseCode = "200", description = "Email sent or queued",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
                           schema = @Schema(implementation = EmailResponse.class)))
    @APIResponse(responseCode = "400", description = "Missing required field 'to'")
    @APIResponse(responseCode = "500", description = "Failed to send or queue email")
    public Response sendEmail(@MultipartForm EmailForm form) {
        if (form.to == null || form.to.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new EmailResponse(false, "Field 'to' is required"))
                    .build();
        }
        try {
            boolean sent = emailService.send(form);
            String msg = sent
                    ? "Email sent successfully to " + form.to
                    : "SMTP not configured — email queued. POST /api/email/send-pending to flush when credentials are set.";
            return Response.ok(new EmailResponse(sent, msg)).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process email to %s", form.to);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new EmailResponse(false, "Error: " + e.getMessage()))
                    .build();
        }
    }

    // ── Flush pending ────────────────────────────────────────────────────────

    @POST
    @Path("/send-pending")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Send all queued emails",
        description = "Sends every email stored in ./pending-emails/. Requires SMTP to be configured."
    )
    public Response sendPending() {
        if (!emailService.isSmtpConfigured()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new EmailResponse(false, "SMTP not configured — set MAIL_USERNAME and MAIL_PASSWORD"))
                    .build();
        }
        try {
            int sent = emailService.flushPending();
            return Response.ok(new EmailResponse(true, "Flushed " + sent + " pending email(s)")).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to flush pending emails");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new EmailResponse(false, "Error: " + e.getMessage()))
                    .build();
        }
    }

    // ── Status / health ──────────────────────────────────────────────────────

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Health check — returns SMTP status and pending count")
    public Response health() {
        boolean smtpOk  = emailService.isSmtpConfigured();
        int     pending = emailService.pendingCount();
        String  msg     = smtpOk
                ? "Email service is running (SMTP configured). Pending: " + pending
                : "Email service is running (SMTP NOT configured — emails will be queued). Pending: " + pending;
        return Response.ok(new EmailResponse(true, msg)).build();
    }
}
