#!/bin/bash
# ================================================================
#  Chaos Game 2D - kompilacja i uruchomienie (Linux / macOS)
#  Wymagania: JDK 11+
# ================================================================

echo "========================================"
echo "  Chaos Game 2D – Wizualizacja Atraktora"
echo "========================================"

mkdir -p out

echo "Kompilacja..."
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
STATUS=$?
rm sources.txt

if [ $STATUS -ne 0 ]; then
    echo "BLAD KOMPILACJI!"
    exit 1
fi

echo "OK – uruchamianie..."
java -cp out chaosGame.Main
