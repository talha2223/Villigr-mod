"""
STT Engine - Speech to Text using Google Speech Recognition
"""

import speech_recognition as sr
import threading

class SttEngine:
    def __init__(self, language="ur-PK"):
        self.language = language
        self.recognizer = sr.Recognizer()
        self.microphone = sr.Microphone()
        self.listening = False
        self.on_result_callback = None
        self._thread = None
        
        # Adjust for ambient noise
        with self.microphone as source:
            self.recognizer.adjust_for_ambient_noise(source, duration=1)
    
    def start_listening(self, callback=None):
        """Start continuous listening in background."""
        self.on_result_callback = callback
        self.listening = True
        
        self._thread = threading.Thread(target=self._listen_loop, daemon=True)
        self._thread.start()
        print("[STT] Started listening...")
    
    def stop_listening(self):
        """Stop continuous listening."""
        self.listening = False
        print("[STT] Stopped listening")
    
    def listen_once(self):
        """Listen for one phrase and return text."""
        try:
            with self.microphone as source:
                print("[STT] Listening...")
                audio = self.recognizer.listen(source, timeout=5, phrase_time_limit=10)
            
            print("[STT] Recognizing...")
            text = self.recognizer.recognize_google(audio, language=self.language)
            print(f"[STT] Recognized: {text}")
            return text
            
        except sr.WaitTimeoutError:
            print("[STT] Timeout - no speech detected")
            return None
        except sr.UnknownValueError:
            print("[STT] Could not understand audio")
            return None
        except sr.RequestError as e:
            print(f"[STT Error] {e}")
            return None
    
    def _listen_loop(self):
        """Continuous listening loop."""
        while self.listening:
            text = self.listen_once()
            if text and self.on_result_callback:
                self.on_result_callback(text)
