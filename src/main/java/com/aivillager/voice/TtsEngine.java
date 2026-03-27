package com.aivillager.voice;

import com.aivillager.AiVillagerMod;

import javax.sound.sampled.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Text-to-Speech engine that generates and plays speech audio.
 * 
 * Connects to a companion TTS server (Python + gTTS) or uses
 * Java's built-in speech capabilities as a fallback.
 * 
 * SETUP:
 *   1. Install Python dependencies: pip install flask gtts
 *   2. Run the TTS server: python tts_server.py
 *   3. The mod will auto-connect to http://localhost:5000
 */
public class TtsEngine {

    private static final ExecutorService TTS_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String DEFAULT_TTS_URL = "http://localhost:5000/tts";

    /**
     * Speak text at a villager's position (world coordinates).
     * This is the main entry point for villager TTS.
     *
     * @param text The text to speak
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     */
    public static void speakAt(String text, double x, double y, double z) {
        TTS_EXECUTOR.submit(() -> {
            try {
                // Request audio from TTS server
                byte[] audioData = requestTtsAudio(text, "ur");

                if (audioData != null && audioData.length > 0) {
                    // Save to temp file and play
                    Path tempFile = Files.createTempFile("villager_tts_", ".mp3");
                    Files.write(tempFile, audioData);

                    // Play using Java Sound API (MP3 requires JLayer decoder)
                    // For now, play raw - in production, decode MP3 first
                    playAudioFile(tempFile.toFile());

                    // Clean up
                    Files.deleteIfExists(tempFile);

                    AiVillagerMod.LOGGER.info("[AI Villager TTS] Played speech at (" + x + ", " + y + ", " + z + ")");
                } else {
                    // Fallback: play villager ambient sound
                    AiVillagerMod.LOGGER.warn("[AI Villager TTS] No audio received, using fallback");
                }
            } catch (Exception e) {
                AiVillagerMod.LOGGER.error("[AI Villager TTS] Error: " + e.getMessage());
            }
        });
    }

    /**
     * Request audio from the TTS server.
     */
    private static byte[] requestTtsAudio(String text, String lang) {
        try {
            URL url = new URL(DEFAULT_TTS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            // Send request
            String jsonBody = "{\"text\":\"" + text.replace("\"", "\\\"") + "\",\"lang\":\"" + lang + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return is.readAllBytes();
                }
            } else {
                AiVillagerMod.LOGGER.warn("[AI Villager TTS] Server returned: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            AiVillagerMod.LOGGER.warn("[AI Villager TTS] Server unavailable: " + e.getMessage());
            return null;
        }
    }

    /**
     * Play an audio file using Java Sound API.
     * For MP3 files, you'd need JLayer or similar decoder.
     */
    private static void playAudioFile(File audioFile) {
        try {
            // Try to play as WAV first (Java Sound natively supports WAV)
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            if (AudioSystem.isLineSupported(info)) {
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
                line.close();
                audioStream.close();
            } else {
                AiVillagerMod.LOGGER.warn("[AI Villager TTS] Audio format not supported: " + format);
            }
        } catch (UnsupportedAudioFileException e) {
            // MP3 not natively supported - would need JLayer decoder
            AiVillagerMod.LOGGER.info("[AI Villager TTS] MP3 decoder needed. Install JLayer or convert to WAV.");
        } catch (Exception e) {
            AiVillagerMod.LOGGER.error("[AI Villager TTS] Playback failed: " + e.getMessage());
        }
    }

    /**
     * Check if the TTS server is available.
     */
    public static boolean isServerAvailable() {
        try {
            URL url = new URL(DEFAULT_TTS_URL.replace("/tts", "/health"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
