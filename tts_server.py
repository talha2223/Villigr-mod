"""
Companion TTS Server for AI Villager Mod.
Run this alongside Minecraft for voice output.

Setup:
    pip install flask gtts
    python tts_server.py

The server listens on port 5000 and accepts POST requests with JSON:
    {"text": "Kya haal hai bhai?", "lang": "ur"}

It returns an MP3 audio file.
"""

from flask import Flask, request, send_file, jsonify
from gtts import gTTS
import tempfile
import os

app = Flask(__name__)

@app.route('/tts', methods=['POST'])
def generate_tts():
    try:
        data = request.get_json()
        text = data.get('text', '')
        lang = data.get('lang', 'ur')

        if not text:
            return jsonify({"error": "No text provided"}), 400

        # Generate speech
        tts = gTTS(text=text, lang=lang, slow=False)

        # Save to temp file
        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3')
        tts.save(temp_file.name)
        temp_file.close()

        return send_file(
            temp_file.name,
            mimetype='audio/mpeg',
            as_attachment=True,
            download_name='villager_speech.mp3'
        )
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "service": "AI Villager TTS"})

if __name__ == '__main__':
    print("[AI Villager TTS] Server starting on http://localhost:5000")
    print("[AI Villager TTS] Install: pip install flask gtts")
    app.run(host='0.0.0.0', port=5000, debug=False)
