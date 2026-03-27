"""
Action Handler - Maps AI actions to Minecraft game commands.
Translates action tags into specific Minecraft commands.
"""

# Action mappings to Minecraft commands
ACTION_COMMANDS = {
    "FARM": {
        "description": "Start farming nearby crops",
        "commands": [
            "/tp @e[type=villager,name=Khan Saab,limit=1] ~ ~ ~",
        ],
        "villager_speech": "Haan bhai, fasal ugaata hoon! Khet taiyaar karo!"
    },
    "CHOP_WOOD": {
        "description": "Chop nearby trees",
        "commands": [],
        "villager_speech": "Chalo, lakdi kaat-ta hoon! Ped kahaan hain?"
    },
    "GO_HOME": {
        "description": "Return to village/home",
        "commands": [],
        "villager_speech": "Theek hai, ghar jaata hoon. Raasta batao!"
    },
    "FOLLOW": {
        "description": "Follow the player",
        "commands": [],
        "villager_speech": "Chalo, tumhare peeche aata hoon!"
    },
    "STOP": {
        "description": "Stop current action",
        "commands": [],
        "villager_speech": "Ruk gaya! Ab kya karna hai?"
    },
    "PROTECT": {
        "description": "Protect the player from mobs",
        "commands": [],
        "villager_speech": "Fikar na karo, main hoon na! Koi nahi aayega!"
    },
    "ATTACK": {
        "description": "Attack target mob",
        "commands": [],
        "villager_speech": "Haan maar doon? Pakka? Chalo dekhta hoon!"
    },
    "FLEE": {
        "description": "Run away scared",
        "commands": [],
        "villager_speech": "Bachao bachao! Main nahi marna! Bhagooo!"
    },
    "TRADE": {
        "description": "Trade items with player",
        "commands": [],
        "villager_speech": "Sauda karte hain! Kya chahiye?"
    },
    "DIG": {
        "description": "Start mining/digging",
        "commands": [],
        "villager_speech": "Haan, khodta hoon! Sona milega kya?"
    },
    "BUILD": {
        "description": "Build structure",
        "commands": [],
        "villager_speech": "Ghar banaata hoon! Eent kahaan hain?"
    },
    "EXPLORE": {
        "description": "Explore the area",
        "commands": [],
        "villager_speech": "Chalo dhoondta hoon! Kya milega dekhte hain!"
    }
}

# Item mappings for trades
TRADE_ITEMS = {
    "EMERALD": "minecraft:emerald",
    "BREAD": "minecraft:bread",
    "IRON_SWORD": "minecraft:iron_sword",
    "DIAMOND": "minecraft:diamond",
    "WHEAT": "minecraft:wheat",
    "WOOD": "minecraft:oak_log",
    "COBBLESTONE": "minecraft:cobblestone",
    "TORCH": "minecraft:torch",
    "ARROW": "minecraft:arrow",
    "BOW": "minecraft:bow",
}

class ActionHandler:
    def __init__(self, game_connection=None):
        self.game_connection = game_connection
        self.current_action = None
    
    def execute_action(self, action):
        """
        Execute an action parsed from the AI response.
        
        Args:
            action: dict with 'type', 'item', 'count' keys
        
        Returns:
            tuple: (speech_text, commands_list)
        """
        if not action:
            return None, []
        
        action_type = action.get("type", "").upper()
        
        if action_type not in ACTION_COMMANDS:
            print(f"[Action] Unknown action: {action_type}")
            return None, []
        
        action_info = ACTION_COMMANDS[action_type]
        
        # Handle trade with specific item
        if action_type == "TRADE" and action.get("item"):
            item = action["item"].upper()
            count = action.get("count", 1)
            item_id = TRADE_ITEMS.get(item, f"minecraft:{item.lower()}")
            
            # Generate give command
            give_cmd = f"/give @p {item_id} {count}"
            commands = [give_cmd]
            speech = f"Lo bhai, {count} {item} le lo! Ab meri jaan choro!"
        else:
            commands = list(action_info["commands"])
            speech = action_info["villager_speech"]
        
        self.current_action = action_type
        
        # Send commands to game
        if self.game_connection:
            for cmd in commands:
                self.game_connection.send_action({
                    "type": "COMMAND",
                    "command": cmd
                })
            
            # Send villager action
            self.game_connection.send_villager_action(
                action_type,
                item=action.get("item"),
                count=action.get("count", 1)
            )
        
        return speech, commands
    
    def get_available_actions(self):
        """Return list of available actions."""
        return {k: v["description"] for k, v in ACTION_COMMANDS.items()}
