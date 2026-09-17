package com.aivillager.api;

public class ActionParser {
    public static com.google.gson.JsonObject parseJsonResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) return null;
        try {
            return com.google.gson.JsonParser.parseString(jsonResponse).getAsJsonObject();
        } catch (Exception e) {
            com.aivillager.AiVillagerMod.LOGGER.error("[AI Villager] Failed to parse JSON response: " + e.getMessage());
            return null;
        }
    }

    public static String extractAction(String response) {
        com.google.gson.JsonObject obj = parseJsonResponse(response);
        if (obj != null && obj.has("task")) return obj.get("task").getAsString();
        return null;
    }

    public static String extractMessage(String response) {
        com.google.gson.JsonObject obj = parseJsonResponse(response);
        if (obj != null && obj.has("message")) return obj.get("message").getAsString();
        return "System error: Failed to parse response.";
    }

    public static String extractTarget(String response) {
        com.google.gson.JsonObject obj = parseJsonResponse(response);
        if (obj != null && obj.has("target") && !obj.get("target").isJsonNull()) return obj.get("target").getAsString();
        return null;
    }

    public static java.util.List<net.minecraft.util.math.BlockPos> extractPlan(String response, net.minecraft.util.math.BlockPos origin) {
        com.google.gson.JsonObject obj = parseJsonResponse(response);
        java.util.List<net.minecraft.util.math.BlockPos> plan = new java.util.ArrayList<>();
        if (obj != null && obj.has("plan") && obj.get("plan").isJsonArray()) {
            com.google.gson.JsonArray arr = obj.getAsJsonArray("plan");
            for (com.google.gson.JsonElement el : arr) {
                com.google.gson.JsonObject coord = el.getAsJsonObject();
                int ox = coord.has("x") ? coord.get("x").getAsInt() : 0;
                int oy = coord.has("y") ? coord.get("y").getAsInt() : 0;
                int oz = coord.has("z") ? coord.get("z").getAsInt() : 0;
                plan.add(origin.add(ox, oy, oz));
            }
        }
        return plan;
    }
}
