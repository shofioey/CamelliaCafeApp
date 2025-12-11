@echo off
setlocal

:: ==========================================
:: CONFIGURATION - EDIT THIS PATH
:: Point this to the 'lib' folder of your downloaded JavaFX SDK
:: ==========================================
set PATH_TO_FX="C:\Users\shofi\Semester 3\Pemrograman Berorientasi Objek\UAS_PBO\CamelliaCafeApp\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib"


:: ==========================================
:: COMPILE
:: ==========================================
echo Compiling...
if not exist "out" mkdir out

javac -d out --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml src\*.java src\model\*.java src\data\*.java src\view\*.java

if %errorlevel% neq 0 (
    echo Compilation failed! Please check your JavaFX path in this script.
    pause
    exit /b %errorlevel%
)

:: ==========================================
:: RUN
:: ==========================================
echo Running...
java -cp out --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml Main

pause
