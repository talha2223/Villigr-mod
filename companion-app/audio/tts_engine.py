"""
TTS Engine - Text to Speech using gTTS
"""

import os
import tempfile
import threading
from gtts import gTTS

# Try pygame for audio playback, fallback to playsound
try:
    import pygame
    pygame.mixer.init()
    USE_PYGAME = True
except:
    USE_PYGAME = False
    try:
        from playsound import playsound
    except:
        playsound = None

class TtsEngine:
    def __init__(self, lang="ur"):
        self.lang = lang
        self.speaking = False
        self._queue = []
        self._thread = None
    
    def speak(self, text, blocking=False):
        """
        Convert text to speech and play it.
        Args:
            text: Text to speak
            blocking: If True, wait for speech to finish
        """
        if blocking:
            self._do_speak(text)
        else:
            thread = threading.Thread(target=self._do_speak, args=(text,), daemon=True)
            thread.start()
    
    def _do_speak(self, text):
        """Internal: generate and play speech."""
        self.speaking = True
        temp_file = None
        try:
            # Generate speech
            tts = gTTS(text=text, lang=self.lang, slow=False)
            
            # Save to temp file
            temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3')
            temp_file.close()
            tts.save(temp_file.name)
            
            # Play audio
            if USE_PYGAME:
                pygame.mixer.music.load(temp_file.name)
                pygame.mixer.music.play()
                while pygame.mixer.music.get_busy():
                    pygame.time.wait(100)
            elif playsound:
                playsound(temp_file.name)
            else:
                print("[TTS] No audio backend available!")
                
        except Exception as e:
            print(f"[TTS Error] {e}")
        finally:
            self.speaking = False
            # Clean up temp file
            if temp_file and os.path.exists(temp_file.name):
                try:
                    os.unlink(temp_file.name)
                except:
                    pass
    
    def stop(self):
        """Stop current speech."""
        if USE_PYGAME:
            pygame.mixer.music.stop()
        self.speaking = False
