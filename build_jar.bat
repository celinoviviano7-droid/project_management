@echo off
setlocal enabledelayedexpansion
title Tantagna tetikasa v3 - Exportation JAR

echo ========================================
echo     Tantagna tetikasa v3 - Export JAR   
echo ========================================

set "SCRIPT_ROOT=%~dp0"
set "SRC_DIR=%SCRIPT_ROOT%src"
set "LIB_DIR=%SCRIPT_ROOT%lib"
set "IMG_DIR=%SCRIPT_ROOT%img"
set "BUILD_DIR=%SCRIPT_ROOT%temp_build"
set "FINAL_JAR=%SCRIPT_ROOT%Tantagna_Tetikasa_v3.jar"

rem --- 1. Nettoyage ---
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

rem --- 2. Chercher les outils JDK (javac et jar) ---
set "JAVAC_CMD=javac"
set "JAR_CMD=jar"

rem Tenter de trouver le dossier bin du JDK
for /f "tokens=*" %%i in ('where javac 2^>nul') do (
    set "JAVAC_PATH=%%i"
    set "BIN_DIR=!JAVAC_PATH:\javac.exe=!"
    if exist "!BIN_DIR!\jar.exe" (
        set "JAVAC_CMD=!JAVAC_PATH!"
        set "JAR_CMD=!BIN_DIR!\jar.exe"
    )
)

if "!JAR_CMD!"=="jar" (
    if exist "C:\Program Files\Java\jdk-22\bin\jar.exe" (
        set "JAVAC_CMD=C:\Program Files\Java\jdk-22\bin\javac.exe"
        set "JAR_CMD=C:\Program Files\Java\jdk-22\bin\jar.exe"
    ) else if exist "C:\Program Files\Java\jdk-17\bin\jar.exe" (
        set "JAVAC_CMD=C:\Program Files\Java\jdk-17\bin\javac.exe"
        set "JAR_CMD=C:\Program Files\Java\jdk-17\bin\jar.exe"
    )
)

echo [OK] Outils JDK detectes :
echo      - Javac : %JAVAC_CMD%
echo      - Jar   : %JAR_CMD%

rem --- 3. Compilation ---
echo [..] Compilation des sources...
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\sources_jar.txt"
"%JAVAC_CMD%" -encoding UTF-8 -d "%BUILD_DIR%" -cp "%LIB_DIR%\*" @"%TEMP%\sources_jar.txt"
if errorlevel 1 (
    echo [ERREUR] Compilation echouee.
    pause & exit /b 1
)

rem --- 3. Extraction des dependances (Fat JAR) ---
echo [..] Extraction des bibliotheques...
cd /d "%BUILD_DIR%"
for %%f in ("%LIB_DIR%\*.jar") do (
    echo    - %%~nxf
    "%JAR_CMD%" -xf "%%f"
)
rem Supprimer les manifestes des dependances pour eviter les conflits
if exist "META-INF" (
    if exist "META-INF\MANIFEST.MF" del /q "META-INF\MANIFEST.MF"
)

rem --- 4. Copie des ressources ---
echo [..] Copie des ressources (icons, images)...
cd /d "%SCRIPT_ROOT%"
xcopy /s /e /y "%SRC_DIR%\resources\*" "%BUILD_DIR%\resources\" >nul
xcopy /s /e /y "%IMG_DIR%\*" "%BUILD_DIR%\img\" >nul

rem --- 5. Creation du manifeste ---
echo [..] Generation du Manifeste...
(
echo Manifest-Version: 1.0
echo Main-Class: gestion.Main
echo Created-By: Tantagna Build Script
) > "%TEMP%\manifest.mf"

rem --- 6. Creation du JAR final ---
echo [..] Creation du fichier JAR final...
cd /d "%BUILD_DIR%"
"%JAR_CMD%" -cvfm "%FINAL_JAR%" "%TEMP%\manifest.mf" . >nul
if errorlevel 1 (
    echo [ERREUR] Creation du JAR echouee.
    pause & exit /b 1
)

echo.
echo ========================================
echo [OK] Exportation reussie !
echo Fichier : %FINAL_JAR%
echo ========================================
echo.

rem --- 7. Nettoyage final ---
cd /d "%SCRIPT_ROOT%"
rmdir /s /q "%BUILD_DIR%"
del "%TEMP%\sources_jar.txt"
del "%TEMP%\manifest.mf"

pause
