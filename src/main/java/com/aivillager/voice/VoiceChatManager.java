package com.aivillager.voice;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Voice Chat Manager for AI Villager Mod.
 * 
 * Handles:
 * - Microphone audio capture on client
 * - Audio transmission via Fabric networking
 * - Positional audio playback at villager locations
 * - TTS audio playback from villagers
 * 
 * Architecture:
 * - Client captures mic audio → sends to server as packets
 * - Server relays audio to nearby players
 * - Client plays received audio at correct position
 */
public class VoiceChatManager {

    // Network packet identifiers
    public static final Identifier VOICE_PACKET_ID = new Identifier("aivillager", "voice");
    public static final Identifier TTS_PACKET_ID = new Identifier("aivillager", "tts");
    public static final Identifier VOICE_TOGGLE_ID = new Identifier("aivillager", "voice_toggle");

    // Audio settings
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000.0f,   // Sample rate
            16,          // Sample size in bits
            1,           // Channels (mono)
            2,           // Frame size (bytes)
            16000.0f,   // Frame rate
            false        // Little endian
    );

    private static final int BUFFER_SIZE = 4096;
    private static final double MAX_VOICE_DISTANCE = 32.0; // blocks

    // State
    private static boolean voiceEnabled = false;
    private static TargetDataLine microphone = null;
    private static SourceDataLine speaker = null;
    private static Thread captureThread = null;
    private static final Map<UUID, Queue<byte[]>> playerAudioQueues = new ConcurrentHashMap<>();
    private static final ExecutorService audioExecutor = Executors.newFixedThreadPool(2);

    /**
     * Initialize the voice chat system.
     */
    public static void init() {
        try {
            // Check if audio system is available
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            System.out.println("[AI Villager Voice] Available audio devices: " + mixers.length);
            for (Mixer.Info info : mixers) {
                System.out.println("[AI Villager Voice]   - " + info.getName() + " (" + info.getDescription() + ")");
            }
        } catch (Exception e) {
            System.err.println("[AI Villager Voice] Audio system not available: " + e.getMessage());
        }
    }

    /**
     * Toggle voice chat on/off.
     */
    public static void toggleVoice() {
        if (voiceEnabled) {
            stopCapture();
            voiceEnabled = false;
            System.out.println("[AI Villager Voice] Voice chat DISABLED");
        } else {
            startCapture();
            voiceEnabled = true;
            System.out.println("[AI Villager Voice] Voice chat ENABLED");
        }
    }

    /**
     * Start capturing audio from the microphone.
     */
    public static void startCapture() {
        try {
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("[AI Villager Voice] Microphone not supported!");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(AUDIO_FORMAT);
            microphone.start();

            captureThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (voiceEnabled && microphone.isOpen()) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        // Copy and queue audio data for sending
                        byte[] audioData = Arrays.copyOf(buffer, bytesRead);
                        onAudioCaptured(audioData);
                    }
                }
            }, "AI-Villager-Voice-Capture");
            captureThread.setDaemon(true);
            captureThread.start();

            System.out.println("[AI Villager Voice] Microphone capture started");
        } catch (LineUnavailableException e) {
            System.err.println("[AI Villager Voice] Failed to open microphone: " + e.getMessage());
        }
    }

    /**
     * Stop capturing audio.
     */
    public static void stopCapture() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
        System.out.println("[AI Villager Voice] Microphone capture stopped");
    }

    /**
     * Called when audio is captured from the microphone.
     * Sends the audio data to the server.
     */
    private static void onAudioCaptured(byte[] audioData) {
        // This would send to server via Fabric networking
        // For now, queue it for local processing
        audioExecutor.submit(() -> {
            // Encode and send via network packet
            // In a full implementation, this uses ServerPlayNetworking
        });
    }

    /**
     * Play TTS audio at a villager's position.
     * Called when the villager speaks an AI response.
     *
     * @param audioData Raw PCM audio data
     * @param x Villager X position
     * @param y Villager Y position
     * @param z Villager Z position
     */
    public static void playTtsAt(byte[] audioData, double x, double y, double z) {
        audioExecutor.submit(() -> {
            try {
                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                if (!AudioSystem.isLineSupported(speakerInfo)) {
                    // Fallback: play without positional audio
                    playDirect(audioData);
                    return;
                }

                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                line.open(AUDIO_FORMAT);
                line.start();

                // Apply volume based on distance (simplified positional audio)
                // In a real implementation, you'd calculate distance from listener to (x,y,z)
                float volume = 1.0f;
                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (20.0 * Math.log10(volume));
                    gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(dB, gainControl.getMaximum())));
                }

                line.write(audioData, 0, audioData.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                System.err.println("[AI Villager Voice] Playback error: " + e.getMessage());
            }
        });
    }

    /**
     * Play audio directly (non-positional fallback).
     */
    private static void playDirect(byte[] audioData) {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(AUDIO_FORMAT);
            line.start();
            line.write(audioData, 0, audioData.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            System.err.println("[AI Villager Voice] Direct playback error: " + e.getMessage());
        }
    }

    /**
     * Convert MP3 audio data to PCM for playback.
     * Requires an MP3 decoder library like JLayer.
     * Falls back to raw playback if decoder not available.
     */
    public static byte[] mp3ToPcm(byte[] mp3Data) {
        // For simplicity, we'll use raw playback
        // A full implementation would use JLayer or similar
        return mp3Data;
    }

    public static boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    public static AudioFormat getAudioFormat() {
        return AUDIO_FORMAT;
    }
}
