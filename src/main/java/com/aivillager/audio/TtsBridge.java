package com.aivillager.audio;

import com.aivillager.AiVillagerMod;
import com.aivillager.config.ModConfig;
import com.aivillager.voice.TtsEngine;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.CompletableFuture;

/**
 * Bridge between the villager interaction system and the TTS engine.
 * When a villager speaks, this handles generating and playing the audio.
 */
public class TtsBridge {

    /**
     * Speaks the given text at the villager's position.
     * Runs asynchronously to avoid blocking the game.
     *
     * @param text The text to speak (Roman Urdu)
     * @param villager The villager entity to speak from
     */
    public static void speak(String text, VillagerEntity villager) {
        if (!ModConfig.isTtsEnabled()) {
            return;
        }

        final double x = villager.getX();
        final double y = villager.getY();
        final double z = villager.getZ();

        CompletableFuture.runAsync(() -> {
            try {
                // Check if TTS server is available
                if (TtsEngine.isServerAvailable()) {
                    // Use TTS server for voice output
                    TtsEngine.speakAt(text, x, y, z);
                    AiVillagerMod.LOGGER.info("[AI Villager TTS] Speaking via server: " +
                            text.substring(0, Math.min(50, text.length())));
                } else {
                    // Fallback: play villager ambient sound in-game
                    if (villager.getServer() != null) {
                        villager.getServer().execute(() -> {
                            villager.world.playSound(
                                    null,
                                    villager.getBlockPos(),
                                    SoundEvents.ENTITY_VILLAGER_AMBIENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    0.8F + (float)(Math.random() * 0.4)
                            );
                        });
                    }
                    AiVillagerMod.LOGGER.info("[AI Villager TTS] TTS server unavailable, playing ambient sound");
                }
            } catch (Exception e) {
                AiVillagerMod.LOGGER.error("[AI Villager TTS] Failed: " + e.getMessage());
            }
        });
    }
}
