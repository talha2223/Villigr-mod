package com.aivillager.voice;

import com.aivillager.AiVillagerMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side voice chat initialization.
 * Handles keybinds for voice toggle on the client.
 */
public class VoiceChatClient implements ClientModInitializer {

    private static KeyBinding voiceToggleKey;
    private static KeyBinding voiceSettingsKey;
    private static boolean wasPressed = false;

    @Override
    public void onInitializeClient() {
        // Register keybind: V to toggle voice chat
        voiceToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aivillager.voice_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.aivillager.voice"
        ));

        // Register keybind: Ctrl+V for voice settings
        voiceSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aivillager.voice_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.aivillager.voice"
        ));

        // Handle keybind on client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (voiceToggleKey.wasPressed()) {
                VoiceChatManager.toggleVoice();
                if (client.player != null) {
                    String status = VoiceChatManager.isVoiceEnabled() ? "\u00a7aENABLED" : "\u00a7cDISABLED";
                    client.player.sendMessage(
                            new LiteralText("\u00a7e[AI Villager Voice] " + status),
                            false
                    );
                }
            }
        });

        AiVillagerMod.LOGGER.info("[AI Villager Voice] Client-side voice chat initialized. Press V to toggle.");
    }
}
