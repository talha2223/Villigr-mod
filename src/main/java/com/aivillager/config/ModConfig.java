package com.aivillager.config;

import com.aivillager.AiVillagerMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles loading and saving the mod configuration.
 * API key is stored in .minecraft/config/aivillager.json
 */
public class ModConfig {

    private static final Path CONFIG_PATH = Paths.get("config", "aivillager.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ConfigData data = new ConfigData();

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                    data = GSON.fromJson(reader, ConfigData.class);
                    if (data == null) {
                        data = new ConfigData();
                    }
                }
                AiVillagerMod.LOGGER.info("[AI Villager] Config loaded from " + CONFIG_PATH.toAbsolutePath());
            } else {
                // Create default config file
                save();
                AiVillagerMod.LOGGER.info("[AI Villager] Default config created at " + CONFIG_PATH.toAbsolutePath());
                AiVillagerMod.LOGGER.warn("[AI Villager] Please add your Google Gemini API key to the config file!");
            }
        } catch (IOException e) {
            AiVillagerMod.LOGGER.error("[AI Villager] Failed to load config!", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            AiVillagerMod.LOGGER.error("[AI Villager] Failed to save config!", e);
        }
    }

    public static String getApiKey() {
        return data.apiKey;
    }

    public static String getGeminiModel() {
        return data.geminiModel != null ? data.geminiModel : "gemini-2.0-flash";
    }

    public static String getTtsServerUrl() {
        return data.ttsServerUrl != null ? data.ttsServerUrl : "http://localhost:5000/tts";
    }

    public static boolean isTtsEnabled() {
        return data.ttsEnabled;
    }

    private static class ConfigData {
        // IMPORTANT: Never commit your real API key!
        // Get your key from: https://aistudio.google.com/apikey
        String apiKey = "AIzaSyAFNTLb5WLnDaW-M8S4bWrmEbuG0AFoK9Y";
        String geminiModel = "gemini-2.0-flash";
        String ttsServerUrl = "http://localhost:5000/tts";
        boolean ttsEnabled = true;
    }
}
