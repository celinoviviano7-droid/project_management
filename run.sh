#!/bin/bash
# GestionPro v3 - Script de lancement Linux/macOS
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src"
OUT_DIR="$SCRIPT_DIR/out"

# Couleurs
GREEN='\033[0;32m'; CYAN='\033[0;36m'; RED='\033[0;31m'; NC='\033[0m'

echo -e "${CYAN}╔══════════════════════════════════════╗${NC}"
echo -e "${CYAN}║       GestionPro v3 — Lancement      ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════╝${NC}"
echo ""

# Vérifier Java
if ! command -v java &>/dev/null; then
    echo -e "${RED}[ERREUR] Java n'est pas installé.${NC}"
    echo "Téléchargez Java 17+ sur https://adoptium.net"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo -e "${GREEN}[✓] Java $JAVA_VER détecté${NC}"

# Chercher javac
JAVAC=""
for JVM_DIR in /usr/lib/jvm/java-21* /usr/lib/jvm/java-17* /usr/lib/jvm/java-*; do
    if [ -f "$JVM_DIR/bin/javac" ]; then JAVAC="$JVM_DIR/bin/javac"; break; fi
done
[ -z "$JAVAC" ] && JAVAC=$(which javac 2>/dev/null || true)

if [ -z "$JAVAC" ]; then
    echo -e "${RED}[ERREUR] javac (compilateur) introuvable.${NC}"
    echo "Installez le JDK 17+ : sudo apt install openjdk-17-jdk"
    exit 1
fi

echo -e "${GREEN}[✓] Compilateur : $JAVAC${NC}"

# Compiler
echo -e "${CYAN}[...] Compilation...${NC}"
rm -rf "$OUT_DIR" && mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > /tmp/gp3_sources.txt
$JAVAC -encoding UTF-8 -d "$OUT_DIR" @/tmp/gp3_sources.txt

echo -e "${GREEN}[✓] Compilation réussie${NC}"
echo -e "${CYAN}[...] Démarrage de GestionPro...${NC}"
echo ""
java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -cp "$OUT_DIR" gestion.Main
