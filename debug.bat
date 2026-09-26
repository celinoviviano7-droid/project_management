@echo off
cd /d "%~dp0"
title GestionPro v3 - DEBUG

set "SCRIPT_ROOT=%~dp0"
set "OUT_DIR=%SCRIPT_ROOT%out"
set "SRC_DIR=%SCRIPT_ROOT%src"
set "LIB_DIR=%SCRIPT_ROOT%lib"
set "LOG=%SCRIPT_ROOT%debug_output.txt"

echo ========================================
echo      GestionPro v3 - Mode DEBUG
echo ========================================

echo Demarrage Java... > "%LOG%"
echo Classpath : %OUT_DIR%;%SRC_DIR%;%LIB_DIR%\* >> "%LOG%"
echo. >> "%LOG%"

java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true ^
     -Dsun.java2d.noddraw=true ^
     -Dsun.java2d.d3d=false ^
     -Dsun.java2d.opengl=false ^
     -cp "%OUT_DIR%;%SRC_DIR%;%LIB_DIR%\*" ^
     gestion.Main >> "%LOG%" 2>&1

echo. >> "%LOG%"
echo Exit code: %errorlevel% >> "%LOG%"

echo.
echo === Sortie / Erreurs ===
type "%LOG%"
echo.
echo Le fichier de log est : debug_output.txt
pause
