package com.aivillager.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses action tags from AI-generated responses.
 * Extracts [ACTION:XXX] tags and returns clean messages.
 */
public class ActionParser {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[ACTION:(\\w+)\\]");

    /**
     * Extracts the action tag from the response.
     * @return The action string (FOLLOW, PROTECT, TRADE) or null if no action found
     */
    public static String extractAction(String response) {
        if (response == null) return null;

        Matcher matcher = ACTION_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Returns the response text with action tags removed.
     */
    public static String extractMessage(String response) {
        if (response == null) return "";

        // Remove all action tags
        return ACTION_PATTERN.matcher(response).replaceAll("").trim();
    }
}
