@echo off
set "JAR_PATH=C:\Users\ceo\Desktop\job\GestionProV3\Tantagna_Tetikasa_v3.jar"
echo --- Icons check ---
"C:\Program Files\Java\jdk-22\bin\jar.exe" -tf "%JAR_PATH%" | findstr "resources/icons/plus.svg"
echo --- FlatSVGIcon check ---
"C:\Program Files\Java\jdk-22\bin\jar.exe" -tf "%JAR_PATH%" | findstr "FlatSVGIcon.class"
echo --- JSVG check ---
"C:\Program Files\Java\jdk-22\bin\jar.exe" -tf "%JAR_PATH%" | findstr "weisj"
