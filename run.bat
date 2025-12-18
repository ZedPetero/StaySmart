@echo off
echo Checking for Maven...
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in your PATH.
    echo Please install Maven to run this application.
    pause
    exit /b
)

echo Maven found. Starting StaySmart Application...
echo This may take a moment to download dependencies on the first run.

call mvn clean compile javafx:run
if %errorlevel% neq 0 (
    echo.
    echo An error occurred while running the application.
    pause
)
