package com.aivillager.api;

import com.aivillager.AiVillagerMod;
import com.aivillager.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiApiHandler {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final Gson GSON = new Gson();
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static String buildRequestBody(String villagerName, String playerMessage) {
        JsonObject root = new JsonObject();
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemText = new JsonObject();
        systemText.addProperty("text",
                "You are an autonomous AI Minecraft Villager named " + villagerName + ". " +
                "You must respond in a structured JSON format. " +
                "You have access to tools and must choose an action. " +
                "Keep your 'message' property in funny Roman Urdu (like a Pakistani village elder). " +
                "Your objective is to help the player, gather resources, fight, and survive.\n\n" +
                "Available tasks: \n" +
                "- none: Just talk\n" +
                "- follow: Follow the player\n" +
                "- protect: Protect the player and attack hostile mobs\n" +
                "- mine_resource: Mine a specific block (provide 'target')\n" +
                "- gather_item: Gather a dropped item (provide 'target')\n" +
                "- guard: Guard the current location\n" +
                "- flee: Run away from danger\n" +
                "- build: Build a structure (provide block offsets in 'plan')\n\n" +
                "Respond EXACTLY in this JSON schema:\n" +
                "{\n  \"message\": \"<Roman Urdu response>\",\n  \"task\": \"<task_name>\",\n  \"target\": \"<optional_target_id>\",\n  \"plan\": [\n    {\"x\": 0, \"y\": 0, \"z\": 1}\n  ]\n}");
        systemParts.add(systemText);
        systemInstruction.add("parts", systemParts);
        root.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "Player says: " + playerMessage);
        parts.add(userPart);
        content.addProperty("role", "user");
        content.add("parts", parts);
        contents.add(content);
        root.add("contents", contents);

        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.7);
        genConfig.addProperty("responseMimeType", "application/json");
        root.add("generationConfig", genConfig);
        return GSON.toJson(root);
    }

    public static String generateResponseWithContext(String villagerName, String playerMessage, JsonObject worldContext) {
        String apiKey = ModConfig.getApiKey();
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) return null;
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + ModConfig.getGeminiModel() + ":generateContent?key=" + apiKey;
        try {
            String fullMessage = "Context:\n" + GSON.toJson(worldContext) + "\n\n" + playerMessage;
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(buildRequestBody(villagerName, fullMessage), JSON_TYPE))
                    .addHeader("Content-Type", "application/json")
                    .build();
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;
                return parseResponse(response.body() != null ? response.body().string() : "");
            }
        } catch (IOException e) {
            return null;
        }
    }

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
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
