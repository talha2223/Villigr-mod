package com.aivillager.audio;

import com.aivillager.AiVillagerMod;
import com.aivillager.config.ModConfig;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.CompletableFuture;

/**
 * Bridge for villager voice output.
 * Plays villager ambient sound when they speak.
 * For full TTS, use the companion Python app.
 */
public class TtsBridge {

    /**
     * Speaks the given text at the villager's position.
     * Plays ambient sound as placeholder. Full TTS via companion app.
     */
    public static void speak(String text, VillagerEntity villager) {
        if (!ModConfig.isTtsEnabled()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
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
                AiVillagerMod.LOGGER.info("[AI Villager] Playing ambient sound for: " +
                        text.substring(0, Math.min(50, text.length())));
            } catch (Exception e) {
                AiVillagerMod.LOGGER.error("[AI Villager TTS] Failed: " + e.getMessage());
            }
        });
    }
}
