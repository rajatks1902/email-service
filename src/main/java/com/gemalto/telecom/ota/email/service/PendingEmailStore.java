package com.gemalto.telecom.ota.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gemalto.telecom.ota.email.api.model.PendingEmailRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persists unsent emails as JSON files in ./pending-emails/ (relative to the working directory).
 * Used as a fallback when SMTP credentials are not yet configured.
 */
@ApplicationScoped
public class PendingEmailStore {

    private static final Logger LOG  = Logger.getLogger(PendingEmailStore.class);
    private static final Path   DIR  = Paths.get("pending-emails");
    private final ObjectMapper  json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public String save(PendingEmailRequest request) {
        try {
            Files.createDirectories(DIR);
            String id   = UUID.randomUUID().toString();
            request.id  = id;
            Path   file = DIR.resolve(id + ".json");
            json.writeValue(file.toFile(), request);
            LOG.infof("Email queued → %s", file.toAbsolutePath());
            return id;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save pending email", e);
        }
    }

    public List<PendingEmailRequest> loadAll() {
        List<PendingEmailRequest> list = new ArrayList<>();
        if (!Files.exists(DIR)) return list;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(DIR, "*.json")) {
            for (Path p : ds) {
                try {
                    list.add(json.readValue(p.toFile(), PendingEmailRequest.class));
                } catch (IOException e) {
                    LOG.warnf("Skipping unreadable pending email file: %s", p);
                }
            }
        } catch (IOException e) {
            LOG.warn("Could not list pending emails", e);
        }
        return list;
    }

    public void delete(String id) {
        try {
            Files.deleteIfExists(DIR.resolve(id + ".json"));
        } catch (IOException e) {
            LOG.warnf("Could not delete pending email %s: %s", id, e.getMessage());
        }
    }

    public int count() {
        if (!Files.exists(DIR)) return 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(DIR, "*.json")) {
            int n = 0;
            for (Path ignored : ds) n++;
            return n;
        } catch (IOException e) {
            return 0;
        }
    }
}
