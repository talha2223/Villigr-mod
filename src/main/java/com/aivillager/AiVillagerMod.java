package com.aivillager;

import com.aivillager.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AiVillagerMod implements ModInitializer {

    public static final String MOD_ID = "aivillager";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[AI Villager] Mod loading started...");
        ModConfig.load();
        LOGGER.info("[AI Villager] Config loaded. API key configured: " + (ModConfig.getApiKey() != null && !ModConfig.getApiKey().isEmpty()));
        LOGGER.info("[AI Villager] Mod loaded successfully! Villagers are getting smarter...");
        LOGGER.info("[AI Villager] Companion App: Run companion-app/main.py for voice AI");
    }
}
