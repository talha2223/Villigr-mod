# AI Villager Mod 🏘️🤖

A Minecraft Fabric mod (1.16.5) that makes villagers interactive using Google Gemini API for Roman Urdu conversations and actions.

## Features

- **🏷️ Villager Naming** — Each villager gets a random Pakistani/Indian name (Khan Saab, Chaudhry, Ustaad, etc.)
- **💬 AI Chat** — Right-click a villager to talk to them via Google Gemini API
- **🗣️ Roman Urdu** — Villagers respond in funny, rural Roman Urdu personality
- **🎮 Action Tags** — AI can command villagers:
  - `[ACTION:FOLLOW]` — Villager follows you
  - `[ACTION:PROTECT]` — Villager fights hostile mobs near you
  - `[ACTION:TRADE]` — Opens trade (coming soon)
- **🔊 TTS Voice** — Optional text-to-speech output (companion Python server)
- **⚡ Async API** — All API calls use CompletableFuture, no game lag

## Setup

### 1. Get a Gemini API Key
Go to [Google AI Studio](https://aistudio.google.com/apikey) and create an API key.

### 2. Configure the Mod
After first launch, edit `config/aivillager.json`:
```json
{
  "apiKey": "YOUR_ACTUAL_API_KEY_HERE",
  "geminiModel": "gemini-2.0-flash",
  "ttsServerUrl": "http://localhost:5000/tts",
  "ttsEnabled": true
}
```

### 3. Build the Mod
```bash
./gradlew build
```
The mod JAR will be in `build/libs/`.

### 4. (Optional) TTS Server
```bash
pip install flask gtts
python tts_server.py
```

## How to Use

1. **Right-click** a villager → they greet you in Roman Urdu
2. **Type your message** in chat
3. **The villager responds** with AI-generated Roman Urdu dialogue
4. **Ask them to follow/protect** → they'll activate that behavior

### Villager Stats in Protect Mode
When activated for PROTECT, villagers get boosted stats:
- ❤️ 40 HP (normal: 20)
- ⚔️ 8 Attack Damage (normal: 0)
- 🛡️ 10 Armor (normal: 0)

This makes them actually useful in combat instead of dying instantly!

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Fabric Loader | 0.14.22 | Mod loader |
| Fabric API | 0.36.1+1.16 | Modding framework |
| OkHttp | 4.12.0 | HTTP requests to Gemini API |
| Gson | 2.10.1 | JSON parsing (included with MC) |

## Project Structure

```
ai-villager-mod/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── tts_server.py                          # TTS companion server
├── src/main/
│   ├── java/com/aivillager/
│   │   ├── AiVillagerMod.java             # Main mod entry point
│   │   ├── api/
│   │   │   ├── GeminiApiHandler.java      # Gemini API integration
│   │   │   └── ActionParser.java          # Parses [ACTION:*] tags
│   │   ├── audio/
│   │   │   └── TtsBridge.java             # Text-to-speech bridge
│   │   ├── config/
│   │   │   └── ModConfig.java             # Config file handling
│   │   ├── goal/
│   │   │   ├── FollowPlayerGoal.java      # Follow AI goal
│   │   │   └── ProtectPlayerGoal.java     # Protect AI goal
│   │   └── mixin/
│   │       ├── VillagerNameMixin.java     # Assigns names on spawn
│   │       └── VillagerInteractionMixin.java # Right-click handler
│   └── resources/
│       ├── fabric.mod.json                # Mod metadata
│       └── aivillager.mixins.json         # Mixin config
```

## Notes

- **1.16.5 Compatibility**: This mod is specifically built for Minecraft 1.16.5 with Fabric.
- **Villager Combat**: Villagers normally can't fight. The mod boosts their stats when PROTECT mode is activated.
- **API Key Security**: Never commit your API key to public repos! The config file is in `.minecraft/config/`.
- **TTS**: The gTTS server requires Python. Without it, villagers still chat but won't "speak".

## License

MIT
