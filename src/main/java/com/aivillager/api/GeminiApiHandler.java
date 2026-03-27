package com.aivillager.api;

import com.aivillager.AiVillagerMod;
import com.aivillager.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Handles asynchronous communication with Google Gemini API.
 * Uses OkHttp for HTTP requests and Gson for JSON parsing.
 */
public class GeminiApiHandler {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final Gson GSON = new Gson();
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * Sends a player message to Gemini API and returns the villager's response.
     * This method is ASYNCHRONOUS - does not block the game thread.
     *
     * @param villagerName The name of the villager (for personality)
     * @param playerMessage What the player said
     * @return The AI response text, or null on error
     */
    public static String generateResponse(String villagerName, String playerMessage) {
        String apiKey = ModConfig.getApiKey();
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            AiVillagerMod.LOGGER.warn("[AI Villager] API key not configured! Check config/aivillager.json");
            return null;
        }

        String model = ModConfig.getGeminiModel();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        try {
            // Build the request JSON
            String requestBody = buildRequestBody(villagerName, playerMessage);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, JSON_TYPE))
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute synchronously (but this method is called from a CompletableFuture)
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "no body";
                    AiVillagerMod.LOGGER.error("[AI Villager] Gemini API error: " + response.code() + " - " + errorBody);
                    return null;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                return parseResponse(responseBody);
            }
        } catch (IOException e) {
            AiVillagerMod.LOGGER.error("[AI Villager] Failed to call Gemini API: " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds the JSON request body for Gemini API.
     */
    private static String buildRequestBody(String villagerName, String playerMessage) {
        JsonObject root = new JsonObject();

        // Contents
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();

        // System instruction
        JsonArray parts = new JsonArray();

        // System prompt
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text",
                "You are a Minecraft Villager named " + villagerName + ". " +
                "Respond ONLY in Roman Urdu with a funny, rural personality. " +
                "Keep responses short (2-3 sentences max). " +
                "Be like a Pakistani village elder - wise but funny. " +
                "Use words like 'arrey', 'bhai', 'chalo', 'theek hai', 'dekho'. " +
                "If the player asks you to follow them, include [ACTION:FOLLOW] at the end. " +
                "If they ask you to protect them, include [ACTION:PROTECT] at the end. " +
                "If they want to trade, include [ACTION:TRADE] at the end. " +
                "You are in a Minecraft world. Reference blocks, mobs, creepers, etc. humorously."
        );

        // User message
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", playerMessage);

        parts.add(systemPart);
        parts.add(userPart);

        content.addProperty("role", "user");
        content.add("parts", parts);
        contents.add(content);

        root.add("contents", contents);

        // Generation config
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.9);
        genConfig.addProperty("topK", 40);
        genConfig.addProperty("topP", 0.95);
        genConfig.addProperty("maxOutputTokens", 200);
        root.add("generationConfig", genConfig);

        return GSON.toJson(root);
    }

    /**
     * Parses the Gemini API response and extracts the generated text.
     */
    private static String parseResponse(String responseBody) {
        try {
            JsonObject json = GSON.fromJson(responseBody, JsonObject.class);

            if (json.has("candidates")) {
                JsonArray candidates = json.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray responseParts = content.getAsJsonArray("parts");
                            if (responseParts.size() > 0) {
                                return responseParts.get(0).getAsJsonObject().get("text").getAsString();
                            }
                        }
                    }
                }
            }

            AiVillagerMod.LOGGER.warn("[AI Villager] Unexpected response format: " + responseBody);
            return null;
        } catch (Exception e) {
            AiVillagerMod.LOGGER.error("[AI Villager] Failed to parse Gemini response: " + e.getMessage());
            return null;
        }
    }
}
