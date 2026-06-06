package com.gemalto.telecom.ota.email.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {{key}} placeholders in a template string.
 * If a key has no corresponding replacement, the placeholder is left as-is.
 */
@ApplicationScoped
public class TemplateProcessor {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    private static final String APPLICATION_INTRO_TEMPLATE = """
    <p>
        I would like to apply for the <strong>%s</strong> position%s.
        With over 3 years of experience building scalable backend systems and microservices,
        I believe my background aligns well with the requirements of this role.
    </p>
    """;

    private static final String DEFAULT_APPLICATION_INTRO = """
    <p>
        I am reaching out to express my interest in backend engineering opportunities within your organization.
        With over 3 years of experience building scalable backend systems and microservices,
        I believe I can contribute effectively to your engineering team.
    </p>
    """;

    public String process(String template, Map<String, String> replacements) {
        if (template == null) {
            return "";
        }
        if (replacements == null || replacements.isEmpty()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key   = matcher.group(1);
            String value = replacements.getOrDefault(key, matcher.group(0)); // keep {{key}} if not found
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public Map<String, String> populateJobApplicationSection(
            Map<String, String> replacements) {

        Map<String, String> updatedReplacements = new HashMap<>(replacements);

        String jobTitle = replacements.get("jobTitle");
        String jobId = replacements.get("jobId");

        String intro;

        if (jobTitle != null && !jobTitle.isBlank()) {

            String jobIdPart =
                    (jobId != null && !jobId.isBlank())
                            ? " (Job ID: <strong>" + jobId + "</strong>)"
                            : "";

            intro = String.format(
                    APPLICATION_INTRO_TEMPLATE,
                    jobTitle,
                    jobIdPart);

        } else {

            intro = DEFAULT_APPLICATION_INTRO;
        }

        updatedReplacements.put("applicationIntro", intro);

        return updatedReplacements;
    }
}
