"""
Configuration for AI Villager Companion App
"""

import os
import json

CONFIG_FILE = "config.json"

# Default config
DEFAULT_CONFIG = {
    "api_key": "AIzaSyAFNTLb5WLnDaW-M8S4bWrmEbuG0AFoK9Y",
    "gemini_model": "gemini-2.0-flash",
    "tts_language": "ur",
    "voice_speed": 1.0,
    "server_host": "localhost",
    "server_port": 9876,
    "stt_language": "ur-PK",
    "auto_listen": True
}

def load_config():
    """Load configuration from file or create default."""
    if os.path.exists(CONFIG_FILE):
        with open(CONFIG_FILE, "r", encoding="utf-8") as f:
            config = json.load(f)
            # Merge with defaults
            for key, value in DEFAULT_CONFIG.items():
                if key not in config:
                    config[key] = value
            return config
    else:
        save_config(DEFAULT_CONFIG)
        return DEFAULT_CONFIG.copy()

def save_config(config):
    """Save configuration to file."""
    with open(CONFIG_FILE, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2, ensure_ascii=False)
