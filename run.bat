@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if %errorlevel% equ 0 goto maven

echo Maven not found - compiling with javac and the bundled JavaFX SDK instead.
echo The database is the file staysmart.db next to this script (created on first run).
set "FX=%~dp0StaySmart _App\javafx-lib"
if not exist "%FX%\javafx.controls.jar" (
    echo Error: JavaFX SDK not found at "%FX%" and Maven is not installed.
    echo Install Maven from https://maven.apache.org or place the JavaFX SDK jars in that folder.
    pause
    exit /b 1
)

if exist build_out rmdir /s /q build_out
mkdir build_out
dir /s /b "src\*.java" > "build_out\sources.txt"
javac -d build_out --module-path "%FX%" --add-modules javafx.controls,javafx.fxml -cp "lib\*" @"build_out\sources.txt"
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

rem Copy FXML, CSS, images, fonts and the db scripts next to the compiled classes
xcopy /s /q /y /i "src\application" "build_out\application" >nul
del /s /q "build_out\application\*.java" >nul 2>nul

echo Starting StaySmart Application...
java --module-path "%FX%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -Djava.library.path="%FX%" -cp "build_out;lib\*" application.Main
if %errorlevel% neq 0 (
    echo.
    echo An error occurred while running the application.
    pause
)
exit /b

:maven
echo Maven found. Starting StaySmart Application...
echo This may take a moment to download dependencies on the first run.
call mvn clean compile javafx:run
if %errorlevel% neq 0 (
    echo.
    echo An error occurred while running the application.
    pause
)
