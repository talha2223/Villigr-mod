package com.aivillager.voice;

import com.aivillager.AiVillagerMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Handles voice data relay between players on the server.
 * When a player speaks, the audio is sent to nearby players.
 */
public class VoicePacketHandler {

    private static final double VOICE_RANGE = 48.0; // blocks

    /**
     * Register server-side networking handlers.
     */
    public static void register() {
        // Handle incoming voice packets from clients
        ServerPlayNetworking.registerGlobalReceiver(VoiceChatManager.VOICE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            byte[] audioData = buf.readByteArray();

            server.execute(() -> {
                // Relay audio to nearby players
                relayVoice(player, audioData);
            });
        });

        // Handle voice toggle
        ServerPlayNetworking.registerGlobalReceiver(VoiceChatManager.VOICE_TOGGLE_ID, (server, player, handler, buf, responseSender) -> {
            boolean enabled = buf.readBoolean();
            server.execute(() -> {
                String status = enabled ? "enabled" : "disabled";
                AiVillagerMod.LOGGER.info("[AI Villager Voice] " + player.getName().getString() + " " + status + " voice chat");
            });
        });

        AiVillagerMod.LOGGER.info("[AI Villager Voice] Server networking handlers registered");
    }

    /**
     * Relay voice audio from sender to nearby players.
     */
    private static void relayVoice(ServerPlayerEntity sender, byte[] audioData) {
        if (sender.getServer() == null) return;

        Vec3d senderPos = sender.getPos();

        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            // Don't send back to sender
            if (recipient == sender) continue;

            // Check distance
            double distance = senderPos.distanceTo(recipient.getPos());
            if (distance <= VOICE_RANGE) {
                // Calculate volume based on distance (inverse square law)
                float volume = (float) Math.max(0.1, 1.0 - (distance / VOICE_RANGE));

                // Send audio packet to recipient
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeUuid(sender.getUuid());
                buf.writeFloat(volume);
                buf.writeByteArray(audioData);

                ServerPlayNetworking.send(recipient, VoiceChatManager.VOICE_PACKET_ID, buf);
            }
        }
    }

    /**
     * Send TTS audio from a villager to nearby players.
     *
     * @param audioData The audio bytes
     * @param x Villager X position
     * @param y Villager Y position
     * @param z Villager Z position
     */
    public static void sendTtsAudio(byte[] audioData, double x, double y, double z) {
        // This would be called from the server to broadcast TTS audio
        // to players near the villager
        AiVillagerMod.LOGGER.info("[AI Villager Voice] TTS audio ready at (" + x + ", " + y + ", " + z + ")");
    }
}
