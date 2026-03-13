@echo off
REM ================================================================
REM  Chaos Game 2D - kompilacja i uruchomienie (Windows)
REM  Wymagania: JDK 11+ zainstalowany, javac i java dostepne w PATH
REM ================================================================

echo ============================================
echo   Chaos Game 2D - Wizualizacja Atraktora
echo ============================================

if not exist out mkdir out

echo Kompilacja...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo BLAD KOMPILACJI!
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

echo OK - uruchamianie...
java -cp out chaosGame.Main
pause
