@echo off
title Veterinaria San Marcos - Deteniendo
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\detener-todo.ps1"
echo.
pause
