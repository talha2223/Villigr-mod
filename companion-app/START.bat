@echo off
title AI Villager Companion - Khan Saab
color 0A
cls

echo ========================================================
echo   AI Villager Companion App - Khan Saab
echo   For Minecraft 1.16.5 Fabric Mod
echo ========================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found!
    echo.
    echo Please install Python from: https://python.org/downloads
    echo IMPORTANT: Check "Add Python to PATH" during installation!
    echo.
    pause
    exit /b 1
)

echo [OK] Python found!
echo.

REM Install dependencies
echo [1/3] Installing dependencies...
pip install -q google-generativeai gTTS pygame SpeechRecognition websockets playsound 2>nul
if errorlevel 1 (
    echo Trying with --user flag...
    pip install --user -q google-generativeai gTTS pygame SpeechRecognition websockets playsound 2>nul
)
echo [OK] Dependencies installed!
echo.

REM Run the app
echo [2/3] Starting AI Villager Companion...
echo [3/3] Click "Connect" in the app, then join Minecraft!
echo.
echo ========================================================
echo.
python main.py

if errorlevel 1 (
    echo.
    echo [ERROR] App crashed! Check the error above.
    pause
)
