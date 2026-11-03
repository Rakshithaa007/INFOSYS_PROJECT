@echo off
echo =========================================================
echo  Launching Intelligent Bug Diagnosis Platform (Java 21)
echo =========================================================

if not exist "bin\com\bugdiagnosis\Main.class" (
    echo Binaries not found. Triggering build first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

echo Starting HTTP Server and Web Dashboard on http://localhost:8080 ...
java -cp bin com.bugdiagnosis.Main --port 8080
pause