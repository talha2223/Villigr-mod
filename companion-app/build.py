"""
Build script to create standalone EXE using PyInstaller.
Run: python build.py
"""

import subprocess
import sys
import os

def build():
    print("🔨 Building AI Villager Companion App EXE...")
    
    # Check PyInstaller
    try:
        import PyInstaller
    except ImportError:
        print("Installing PyInstaller...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "pyinstaller"])
    
    # Build command
    cmd = [
        sys.executable, "-m", "PyInstaller",
        "--onefile",
        "--windowed",
        "--name", "AI_Villager_Companion",
        "--icon", "NONE",
        "--add-data", "config.py;.",
        "--add-data", "api;api",
        "--add-data", "audio;audio",
        "--add-data", "actions;actions",
        "--add-data", "gui;gui",
        "main.py"
    ]
    
    print(f"Running: {' '.join(cmd)}")
    subprocess.check_call(cmd)
    
    print()
    print("✅ Build complete!")
    print("📁 EXE is in: dist/AI_Villager_Companion.exe")
    print("📋 Copy config.json next to the EXE for settings")

if __name__ == "__main__":
    build()
