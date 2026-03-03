@echo off
echo ========================================
echo   PhishNet Argon2 Demo Runner
echo ========================================
echo.

echo Compiling project...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running Argon2 Demo...
echo.
call mvn exec:java -Dexec.mainClass="util.DemoArgon2" -q

echo.
echo ========================================
echo   Demo Complete!
echo ========================================
pause


