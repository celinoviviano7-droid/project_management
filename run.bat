@echo off

title GestionPro v3

echo.
echo ========================================
echo        GestionPro v3 - Lancement      
echo ========================================
echo.

set SCRIPT_DIR=%~dp0
set SRC_DIR=%SCRIPT_DIR%src
set OUT_DIR=%SCRIPT_DIR%out

rem V?rifier java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java non trouv?. Installez Java 17+ depuis https://adoptium.net
    pause & exit /b 1
)
echo [OK] Java detecte

rem Chercher javac
set JAVAC=
for /d %%d in ("C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk*") do (
    if exist "%%d\bin\javac.exe" set JAVAC=%%d\bin\javac.exe
)
if "%JAVAC%"=="" (
    where javac >nul 2>&1 && set JAVAC=javac
)
if "%JAVAC%"=="" (
    echo [ERREUR] javac introuvable. Installez le JDK 17+.
    pause & exit /b 1
)
echo [OK] Compilateur detecte

rem Compiler
echo [..] Compilation en cours...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\gp3_sources.txt"
"%JAVAC%" -encoding UTF-8 -d "%OUT_DIR%" @"%TEMP%\gp3_sources.txt"
if errorlevel 1 (
    echo [ERREUR] Compilation echouee.
    pause & exit /b 1
)
echo [OK] Compilation reussie

echo [..] Demarrage de GestionPro...
echo.
java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -cp "%OUT_DIR%" gestion.Main
pause
