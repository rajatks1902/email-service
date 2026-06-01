package com.gemalto.telecom.ota.email.util;

import jakarta.enterprise.context.ApplicationScoped;

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
}
