"""
AI Villager Companion App - Entry Point
Double-click to run the companion app.
"""

import sys
import os

# Add current directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gui.app import VillagerApp

def main():
    print("=" * 50)
    print("🏘️  AI Villager Companion App v1.0.0")
    print("   For Minecraft 1.16.5 Fabric Mod")
    print("=" * 50)
    print()
    print("📋 Quick Guide:")
    print("   1. Click 'Connect' to start the server")
    print("   2. Install the mod in Minecraft")
    print("   3. The mod will auto-connect to this app")
    print("   4. Type or speak commands in Roman Urdu!")
    print()
    print("🎤 Voice Commands (examples):")
    print("   'farming kro'      → Villager starts farming")
    print("   'wood kaat kr lao' → Villager chops wood")
    print("   'apnay ghar jao'   → Villager goes home")
    print("   'mere peeche aao'  → Villager follows you")
    print("   'mujhe bachao'     → Villager protects you")
    print("   'mujhey emrald do' → Villager gives emerald")
    print("   'main tumhay marunga' → Villager runs away!")
    print()
    
    app = VillagerApp()
    app.run()

if __name__ == "__main__":
    main()
