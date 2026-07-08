package com.gemalto.telecom.ota.email.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateProcessorTest {

    private final TemplateProcessor processor = new TemplateProcessor();

    @Test
    void processReplacesKnownPlaceholdersAndKeepsUnknownOnes() {
        String result = processor.process(
                "Hello {{name}}, role {{role}}, missing {{unknown}}",
                Map.of("name", "Rajat", "role", "Backend Engineer"));

        assertEquals("Hello Rajat, role Backend Engineer, missing {{unknown}}", result);
    }

    @Test
    void populateJobApplicationSectionUsesJobTitleAndJobIdWhenPresent() {
        Map<String, String> replacements = processor.populateJobApplicationSection(
                Map.of("jobTitle", "Backend Engineer", "jobId", "VAL-123"));

        String intro = replacements.get("applicationIntro");
        assertTrue(intro.contains("<strong>Backend Engineer</strong>"));
        assertTrue(intro.contains("Job ID: <strong>VAL-123</strong>"));
    }
}
