@echo off
echo Running StaySmart Application...

REM Set JavaFX module path (adjust if JavaFX is installed elsewhere)
set JAVAFX_MODULES=--module-path "C:\Program Files\Java\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics

REM Run the application
java %JAVAFX_MODULES% -cp "lib\mysql-connector-j-9.5.0.jar;bin" application.Main

pause
