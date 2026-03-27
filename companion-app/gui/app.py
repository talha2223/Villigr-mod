"""
AI Villager Companion App - Main GUI
A desktop app that connects to the Minecraft mod for voice/AI interaction.
"""

import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import threading
import sys
import os

# Add parent to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import load_config, save_config
from api.gemini import GeminiHandler
from api.game_connection import GameConnection
from audio.tts_engine import TtsEngine
from audio.stt_engine import SttEngine
from actions.action_handler import ActionHandler


class VillagerApp:
    def __init__(self):
        self.config = load_config()
        
        # Initialize components
        self.gemini = None
        self.game = None
        self.tts = None
        self.stt = None
        self.action_handler = None
        
        # State
        self.connected = False
        self.listening = False
        self.villager_name = "Khan Saab"
        
        # Build GUI
        self._build_gui()
        self._init_components()
    
    def _build_gui(self):
        """Build the main GUI window."""
        self.root = tk.Tk()
        self.root.title("🏘️ AI Villager - Khan Saab")
        self.root.geometry("800x700")
        self.root.configure(bg="#1a1a2e")
        
        # Style
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("Title.TLabel", font=("Arial", 18, "bold"), 
                        foreground="#e94560", background="#1a1a2e")
        style.configure("Status.TLabel", font=("Arial", 10),
                        foreground="#16c79a", background="#1a1a2e")
        style.configure("TButton", font=("Arial", 11, "bold"))
        
        # Title
        title_frame = tk.Frame(self.root, bg="#1a1a2e")
        title_frame.pack(fill="x", padx=20, pady=10)
        
        tk.Label(title_frame, text="🏘️ AI Villager - Khan Saab", 
                font=("Arial", 20, "bold"), fg="#e94560", bg="#1a1a2e").pack()
        
        # Status bar
        self.status_var = tk.StringVar(value="❌ Not Connected")
        self.status_label = tk.Label(title_frame, textvariable=self.status_var,
                                     font=("Arial", 10), fg="#16c79a", bg="#1a1a2e")
        self.status_label.pack()
        
        # Chat area
        chat_frame = tk.Frame(self.root, bg="#16213e", bd=2, relief="groove")
        chat_frame.pack(fill="both", expand=True, padx=20, pady=10)
        
        self.chat_display = scrolledtext.ScrolledText(
            chat_frame, wrap="word", font=("Consolas", 11),
            bg="#0f3460", fg="#eaeaea", insertbackground="white",
            state="disabled", height=20
        )
        self.chat_display.pack(fill="both", expand=True, padx=5, pady=5)
        
        # Configure text tags for colors
        self.chat_display.tag_config("player", foreground="#00d9ff", font=("Consolas", 11, "bold"))
        self.chat_display.tag_config("villager", foreground="#ffd700", font=("Consolas", 11, "bold"))
        self.chat_display.tag_config("system", foreground="#16c79a", font=("Consolas", 9, "italic"))
        self.chat_display.tag_config("action", foreground="#ff6b6b", font=("Consolas", 10, "bold"))
        self.chat_display.tag_config("error", foreground="#ff4444")
        
        # Input area
        input_frame = tk.Frame(self.root, bg="#1a1a2e")
        input_frame.pack(fill="x", padx=20, pady=(0, 5))
        
        self.input_field = tk.Entry(
            input_frame, font=("Arial", 13), bg="#0f3460", fg="white",
            insertbackground="white", relief="flat"
        )
        self.input_field.pack(side="left", fill="x", expand=True, ipady=8, padx=(0, 10))
        self.input_field.bind("<Return>", lambda e: self._on_send())
        
        send_btn = tk.Button(
            input_frame, text="📤 Send", font=("Arial", 11, "bold"),
            bg="#e94560", fg="white", relief="flat", padx=15,
            command=self._on_send
        )
        send_btn.pack(side="left", padx=5)
        
        # Voice button
        self.voice_btn = tk.Button(
            input_frame, text="🎤 Mic OFF", font=("Arial", 11, "bold"),
            bg="#555555", fg="white", relief="flat", padx=15,
            command=self._toggle_voice
        )
        self.voice_btn.pack(side="left", padx=5)
        
        # Bottom controls
        control_frame = tk.Frame(self.root, bg="#1a1a2e")
        control_frame.pack(fill="x", padx=20, pady=(0, 10))
        
        tk.Button(
            control_frame, text="🔌 Connect", font=("Arial", 10),
            bg="#16c79a", fg="white", relief="flat", padx=10,
            command=self._connect_game
        ).pack(side="left", padx=5)
        
        tk.Button(
            control_frame, text="🔄 Reset Chat", font=("Arial", 10),
            bg="#555555", fg="white", relief="flat", padx=10,
            command=self._reset_chat
        ).pack(side="left", padx=5)
        
        tk.Button(
            control_frame, text="🔊 Test Voice", font=("Arial", 10),
            bg="#555555", fg="white", relief="flat", padx=10,
            command=self._test_voice
        ).pack(side="left", padx=5)
        
        tk.Button(
            control_frame, text="⚙️ Settings", font=("Arial", 10),
            bg="#555555", fg="white", relief="flat", padx=10,
            command=self._show_settings
        ).pack(side="right", padx=5)
        
        # Quick commands
        quick_frame = tk.Frame(self.root, bg="#1a1a2e")
        quick_frame.pack(fill="x", padx=20, pady=(0, 10))
        
        tk.Label(quick_frame, text="Quick:", font=("Arial", 9),
                fg="#888888", bg="#1a1a2e").pack(side="left", padx=(0, 5))
        
        quick_commands = [
            ("🌾 Farm", "farming kro"),
            ("🪓 Wood", "wood kaat kr lao"),
            ("🏠 Home", "apnay ghar jao"),
            ("🚶 Follow", "mere peeche aao"),
            ("🛡️ Protect", "mujhe bachao"),
            ("🛑 Stop", "ruko"),
            ("💰 Trade", "mujhey emrald do"),
            ("⛏️ Dig", "mine kro"),
        ]
        
        for label, cmd in quick_commands:
            tk.Button(
                quick_frame, text=label, font=("Arial", 8),
                bg="#0f3460", fg="white", relief="flat", padx=6, pady=2,
                command=lambda c=cmd: self._quick_command(c)
            ).pack(side="left", padx=2)
    
    def _init_components(self):
        """Initialize all components."""
        try:
            # Gemini
            self.gemini = GeminiHandler(
                self.config["api_key"],
                self.config["gemini_model"]
            )
            self._log_system("✅ Gemini AI connected")
        except Exception as e:
            self._log_system(f"❌ Gemini error: {e}")
        
        # TTS
        try:
            self.tts = TtsEngine(lang=self.config["tts_language"])
            self._log_system("✅ TTS engine ready")
        except Exception as e:
            self._log_system(f"❌ TTS error: {e}")
        
        # Game connection
        self.game = GameConnection(
            self.config["server_host"],
            self.config["server_port"]
        )
        self.game.on_connect_callback = self._on_game_connect
        self.game.on_disconnect_callback = self._on_game_disconnect
        self.game.on_message_callback = self._on_game_message
        
        # Action handler
        self.action_handler = ActionHandler(self.game)
        
        # STT
        try:
            self.stt = SttEngine(language=self.config["stt_language"])
            self._log_system("✅ Speech recognition ready")
        except Exception as e:
            self.stt = None
            self._log_system(f"⚠️ STT not available: {e}")
        
        # Welcome message
        self._log_villager("Assalam-o-Alaikum bhai! Main Khan Saab hoon. Kya haal hai?")
        
        if self.tts:
            threading.Thread(
                target=lambda: self.tts.speak("Assalam o Alaikum bhai! Main Khan Saab hoon!"),
                daemon=True
            ).start()
    
    def _connect_game(self):
        """Start game server for mod connection."""
        if not self.connected:
            self.game.start_background()
            self._log_system("🔄 Waiting for Minecraft mod to connect...")
            self._log_system(f"📡 Server on ws://{self.config['server_host']}:{self.config['server_port']}")
        else:
            self._log_system("Already connected!")
    
    def _on_game_connect(self):
        """Called when Minecraft mod connects."""
        self.connected = True
        self.root.after(0, lambda: self.status_var.set("✅ Connected to Minecraft"))
        self._log_system("🎮 Minecraft mod connected!")
    
    def _on_game_disconnect(self):
        """Called when Minecraft mod disconnects."""
        self.connected = False
        self.root.after(0, lambda: self.status_var.set("❌ Disconnected"))
        self._log_system("⚠️ Minecraft mod disconnected")
    
    def _on_game_message(self, data):
        """Handle messages from Minecraft mod."""
        msg_type = data.get("type", "")
        
        if msg_type == "PLAYER_MESSAGE":
            player_msg = data.get("message", "")
            self.root.after(0, lambda: self._handle_player_message(player_msg))
        
        elif msg_type == "VILLAGER_STATE":
            self.root.after(0, lambda: self._log_system(f"📊 Villager state: {data}"))
    
    def _on_send(self):
        """Handle send button."""
        text = self.input_field.get().strip()
        if not text:
            return
        
        self.input_field.delete(0, "end")
        self._handle_player_message(text)
    
    def _handle_player_message(self, text):
        """Process player message through AI."""
        self._log_player(text)
        
        # Send to Gemini in background
        def process():
            try:
                response, action = self.gemini.send_message(text)
                
                # Log and speak response
                self.root.after(0, lambda: self._log_villager(response))
                
                if self.tts:
                    self.tts.speak(response)
                
                # Execute action
                if action:
                    speech, commands = self.action_handler.execute_action(action)
                    action_text = f"⚡ Action: {action['type']}"
                    if action.get("item"):
                        action_text += f" ({action['item']} x{action.get('count', 1)})"
                    self.root.after(0, lambda: self._log_action(action_text))
                    
                    if speech:
                        self.root.after(0, lambda: self._log_villager(speech))
                        if self.tts:
                            self.tts.speak(speech)
                            
            except Exception as e:
                self.root.after(0, lambda: self._log_system(f"❌ Error: {e}"))
        
        threading.Thread(target=process, daemon=True).start()
    
    def _toggle_voice(self):
        """Toggle voice input."""
        if not self.stt:
            messagebox.showwarning("Warning", "Speech recognition not available!\nInstall PyAudio: pip install pyaudio")
            return
        
        if self.listening:
            self.stt.stop_listening()
            self.listening = False
            self.voice_btn.config(text="🎤 Mic OFF", bg="#555555")
            self._log_system("🎤 Microphone OFF")
        else:
            self.listening = True
            self.voice_btn.config(text="🔴 Listening...", bg="#e94560")
            self._log_system("🎤 Microphone ON - Speak now!")
            
            def on_speech(text):
                self.root.after(0, lambda: self._handle_player_message(text))
            
            self.stt.start_listening(callback=on_speech)
    
    def _quick_command(self, command):
        """Send a quick command."""
        self.input_field.delete(0, "end")
        self.input_field.insert(0, command)
        self._on_send()
    
    def _reset_chat(self):
        """Reset the AI conversation."""
        if self.gemini:
            self.gemini.reset_chat()
            self._log_system("🔄 Chat reset!")
            self._log_villager("Sab bhool gaya! Dobara bolo kya karna hai?")
    
    def _test_voice(self):
        """Test TTS."""
        if self.tts:
            self._log_system("🔊 Testing voice...")
            self.tts.speak("Assalam o Alaikum bhai! Main Khan Saab hoon. Kya haal hai?")
        else:
            messagebox.showwarning("Warning", "TTS not available!")
    
    def _show_settings(self):
        """Show settings dialog."""
        settings = tk.Toplevel(self.root)
        settings.title("Settings")
        settings.geometry("400x300")
        settings.configure(bg="#1a1a2e")
        
        tk.Label(settings, text="⚙️ Settings", font=("Arial", 16, "bold"),
                fg="#e94560", bg="#1a1a2e").pack(pady=10)
        
        # API Key
        tk.Label(settings, text="Gemini API Key:", fg="white", bg="#1a1a2e").pack()
        api_entry = tk.Entry(settings, width=50, show="*")
        api_entry.insert(0, self.config["api_key"])
        api_entry.pack(pady=5)
        
        # Server Port
        tk.Label(settings, text="Server Port:", fg="white", bg="#1a1a2e").pack()
        port_entry = tk.Entry(settings, width=50)
        port_entry.insert(0, str(self.config["server_port"]))
        port_entry.pack(pady=5)
        
        # TTS Language
        tk.Label(settings, text="TTS Language:", fg="white", bg="#1a1a2e").pack()
        lang_var = tk.StringVar(value=self.config["tts_language"])
        lang_menu = ttk.Combobox(settings, textvariable=lang_var, 
                                  values=["ur", "hi", "en", "ar"], width=47)
        lang_menu.pack(pady=5)
        
        def save():
            self.config["api_key"] = api_entry.get()
            self.config["server_port"] = int(port_entry.get())
            self.config["tts_language"] = lang_var.get()
            save_config(self.config)
            self._log_system("✅ Settings saved!")
            settings.destroy()
        
        tk.Button(settings, text="💾 Save", font=("Arial", 12, "bold"),
                 bg="#16c79a", fg="white", relief="flat", padx=20, pady=5,
                 command=save).pack(pady=20)
    
    # Logging helpers
    def _log_player(self, text):
        self.chat_display.config(state="normal")
        self.chat_display.insert("end", f"👤 You: ", "player")
        self.chat_display.insert("end", f"{text}\n")
        self.chat_display.see("end")
        self.chat_display.config(state="disabled")
    
    def _log_villager(self, text):
        self.chat_display.config(state="normal")
        self.chat_display.insert("end", f"🏘️ {self.villager_name}: ", "villager")
        self.chat_display.insert("end", f"{text}\n")
        self.chat_display.see("end")
        self.chat_display.config(state="disabled")
    
    def _log_system(self, text):
        self.chat_display.config(state="normal")
        self.chat_display.insert("end", f"[{text}]\n", "system")
        self.chat_display.see("end")
        self.chat_display.config(state="disabled")
    
    def _log_action(self, text):
        self.chat_display.config(state="normal")
        self.chat_display.insert("end", f"{text}\n", "action")
        self.chat_display.see("end")
        self.chat_display.config(state="disabled")
    
    def run(self):
        """Start the application."""
        self.root.mainloop()


if __name__ == "__main__":
    app = VillagerApp()
    app.run()
