@echo off
title Setup AI Villager Companion
color 0B
cls

echo ========================================================
echo   AI Villager Companion - Auto Setup
echo ========================================================
echo.

echo [1/4] Checking Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo Python not found! Downloading...
    echo.
    echo Opening Python download page...
    start https://www.python.org/downloads/
    echo.
    echo 1. Download Python 3.11+
    echo 2. During install, CHECK "Add Python to PATH"
    echo 3. Run this setup again after installing Python
    pause
    exit /b 1
)

for /f "tokens=2" %%i in ('python --version 2^>^&1') do set PYVER=echo %%i
echo Python found!
echo.

echo [2/4] Upgrading pip...
python -m pip install --upgrade pip >nul 2>&1
echo.

echo [3/4] Installing packages (this may take a minute)...
echo   - google-generativeai (Gemini AI)
pip install google-generativeai
echo   - gTTS (Text-to-Speech)
pip install gTTS
echo   - pygame (Audio playback)
pip install pygame
echo   - SpeechRecognition (Voice input)
pip install SpeechRecognition
echo   - websockets (Game connection)
pip install websockets
echo   - playsound (Audio fallback)
pip install playsound
echo.

echo [4/4] Testing imports...
python -c "import google.generativeai; import gtts; import pygame; import speech_recognition; import websockets; print('All packages OK!')" 2>nul
if errorlevel 1 (
    echo [WARNING] Some packages may have issues. App will try anyway.
) else (
    echo [OK] All packages installed successfully!
)
echo.
echo ========================================================
echo   Setup complete! Run START.bat to launch the app.
echo ========================================================
pause
