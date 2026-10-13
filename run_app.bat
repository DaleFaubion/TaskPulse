@echo off
title TaskPulse Launcher
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_app.ps1"
pause
