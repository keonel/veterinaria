@echo off
title Veterinaria San Marcos - Iniciando
echo ============================================
echo   Veterinaria San Marcos
echo   Iniciando el sistema, por favor espera...
echo ============================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\iniciar-todo.ps1"
if errorlevel 1 (
  echo.
  echo Ocurrio un error. Lee el mensaje de arriba.
)
echo.
pause
