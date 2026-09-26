@echo off
cd /d "%~dp0"
title GestionPro v3 - Lancement

echo ========================================
echo        GestionPro v3 - Lancement      
echo ========================================

set "SCRIPT_ROOT=%~dp0"
set "SRC_DIR=%SCRIPT_ROOT%src"
set "OUT_DIR=%SCRIPT_ROOT%out"
set "LIB_DIR=%SCRIPT_ROOT%lib"

rem --- Forcer le JDK 64-bit (evite le crash avec la JVM 32-bit) ---
set "JAVA_CMD=java"
set "JAVAC_CMD=javac"

if exist "C:\Program Files\Java\jdk-22\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-22\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Java\jdk-22\bin\javac.exe"
    goto :jdk_found
)
if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-17\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Java\jdk-17\bin\javac.exe"
    goto :jdk_found
)
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot\bin\java.exe"
    set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot\bin\javac.exe"
    goto :jdk_found
)

:jdk_found
echo [OK] Java : %JAVA_CMD%
echo [OK] Compilateur : %JAVAC_CMD%

rem --- Verifier que le JDK est bien 64-bit ---
"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java non trouve ou inaccessible.
    pause & exit /b 1
)

rem --- Compilation ---
echo [..] Compilation...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\gp3_sources.txt"
"%JAVAC_CMD%" -encoding UTF-8 -d "%OUT_DIR%" -cp "%LIB_DIR%\*" @"%TEMP%\gp3_sources.txt"
if errorlevel 1 (
    echo [ERREUR] Compilation echouee.
    pause & exit /b 1
)
echo [OK] Compilation reussie.

rem --- Execution ---
echo [..] Demarrage...
"%JAVA_CMD%" -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true ^
     -Dsun.java2d.noddraw=true ^
     -Dsun.java2d.d3d=false ^
     -cp "%OUT_DIR%;%SRC_DIR%;%LIB_DIR%\*" gestion.Main
if errorlevel 1 (
    echo [ERREUR] L'application s'est arretee avec le code %errorlevel%.
)
echo.
pause
