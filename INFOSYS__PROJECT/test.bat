@echo off
echo =========================================================
echo  Running Milestone 2.4 Accuracy Validation & Benchmark
echo =========================================================

if not exist "bin\com\bugdiagnosis\Main.class" (
    call build.bat
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

java -cp bin com.bugdiagnosis.Main --benchmark
pause