"""
Gemini API Handler for AI Villager Companion App.
Handles conversation with the villager AI personality.
"""

import google.generativeai as genai
import json
import re

SYSTEM_PROMPT = """You are a Minecraft Villager named "Khan Saab". You live in a Minecraft world.

IMPORTANT RULES:
1. You ONLY understand and respond in Roman Urdu - funny, rural personality
2. You are a Pakistani village elder - wise but funny
3. Keep responses SHORT (1-3 sentences max)
4. Use words like: arrey, bhai, chalo, theek hai, dekho, yaar, jani

ACTION SYSTEM - You MUST include actions when the player asks you to do something:

If player says "farming kro", "fasal ugaao", "farm banao" → respond with [ACTION:FARM]
If player says "wood kaat", "lakdi kaat", "tree tod" → respond with [ACTION:CHOP_WOOD]  
If player says "go home", "ghar jao", "apnay ghar jao" → respond with [ACTION:GO_HOME]
If player says "follow me", "mere peeche aao", "mere sath aao" → respond with [ACTION:FOLLOW]
If player says "stop", "ruko", "idhar ruk" → respond with [ACTION:STOP]
If player says "protect me", "bachao", "mujhe bachao" → respond with [ACTION:PROTECT]
If player says "attack", "maar", "ussko maar" → respond with [ACTION:ATTACK]
If player says "give me emerald", "emrald do", "trade kro" → respond with [ACTION:TRADE]
If player says "dig", "khod", "mine kro" → respond with [ACTION:DIG]
If player says "build", "banao", "ghar banao" → respond with [ACTION:BUILD]
If player says "explore", "dhoondo", "nikaal" → respond with [ACTION:EXPLORE]

EMOTIONAL RESPONSES:
- If player threatens "main tumhay marunga" → React scared, run away [ACTION:FLEE]
- If player promises reward "emrald doga" → React happy, cooperate
- If player is nice "acha bhai" → React grateful

TRADE SYSTEM:
- You can trade! If asked for items, say what you want in return
- "10 emerald do" → [ACTION:TRADE:EMERALD:10]
- "food do" → [ACTION:TRADE:BREAD:5]
- "sword do" → [ACTION:TRADE:IRON_SWORD:1]

Always stay in character. You are a Minecraft villager living your life.
Reference blocks, mobs, creepers, zombies humorously."""

class GeminiHandler:
    def __init__(self, api_key, model_name="gemini-2.0-flash"):
        self.api_key = api_key
        self.model_name = model_name
        self.chat_history = []
        
        # Configure Gemini
        genai.configure(api_key=api_key)
        
        # Create model with system instruction
        self.model = genai.GenerativeModel(
            model_name=model_name,
            system_instruction=SYSTEM_PROMPT,
            generation_config={
                "temperature": 0.9,
                "top_p": 0.95,
                "top_k": 40,
                "max_output_tokens": 300,
            }
        )
        
        # Start chat session
        self.chat = self.model.start_chat(history=[])
    
    def send_message(self, player_message):
        """
        Send a player message to Gemini and get villager response.
        Returns: (response_text, action_dict)
        """
        try:
            response = self.chat.send_message(player_message)
            response_text = response.text
            
            # Parse actions from response
            action = self._parse_action(response_text)
            
            # Clean response (remove action tags)
            clean_text = self._clean_response(response_text)
            
            return clean_text, action
            
        except Exception as e:
            print(f"[Gemini Error] {e}")
            return "Arrey yaar, kuch ghalti ho gayi! Dobara bolo.", None
    
    def _parse_action(self, text):
        """Parse action tags from response."""
        action_pattern = r'\[ACTION:(\w+)(?::(\w+):(\d+))?\]'
        match = re.search(action_pattern, text)
        
        if match:
            action = {
                "type": match.group(1),
                "item": match.group(2),
                "count": int(match.group(3)) if match.group(3) else 1
            }
            return action
        return None
    
    def _clean_response(self, text):
        """Remove action tags from response."""
        return re.sub(r'\[ACTION:\w+(?::\w+:\d+)?\]', '', text).strip()
    
    def reset_chat(self):
        """Reset the conversation."""
        self.chat = self.model.start_chat(history=[])
