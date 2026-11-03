@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo  Building Intelligent Bug Diagnosis Platform (Java 21)
echo =========================================================

if not exist "bin" mkdir bin

echo Scanning Java source files...
dir /s /b src\main\java\*.java > sources.txt

echo Compiling Java source files...
javac -encoding UTF-8 -d bin @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

if exist "sources.txt" del sources.txt

echo.
echo [SUCCESS] Compilation completed successfully! Binaries in bin\
pause