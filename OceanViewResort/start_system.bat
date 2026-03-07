@echo off
setlocal EnableDelayedExpansion
title OceanView Resort — Deployment Script

:: ============================================================
::  Enable Virtual Terminal (ANSI color) — Windows 10+
:: ============================================================
for /F %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
set "GREEN=!ESC![92m"
set "RED=!ESC![91m"
set "YELLOW=!ESC![93m"
set "CYAN=!ESC![96m"
set "WHITE=!ESC![97m"
set "BOLD=!ESC![1m"
set "DIM=!ESC![2m"
set "RESET=!ESC![0m"

:: ============================================================
::  STEP 0 — PATH CONFIGURATION
::  Adjust these variables to match your machine.
:: ============================================================
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "MAVEN_HOME=C:\Program Files\NetBeans-24\netbeans\java\maven"
set "TOMCAT_HOME=C:\xampp\tomcat"
set "PROJECT_DIR=%~dp0"
set "WAR_NAME=OceanViewResort"
set "APP_URL=http://localhost:8080/%WAR_NAME%"

:: Add Java and Maven to PATH for this session
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

:: ============================================================
::  BANNER
:: ============================================================
echo.
echo !CYAN!!BOLD!  ╔══════════════════════════════════════════════════╗!RESET!
echo !CYAN!!BOLD!  ║      OceanView Resort — Auto Deploy Script        ║!RESET!
echo !CYAN!!BOLD!  ╚══════════════════════════════════════════════════╝!RESET!
echo !DIM!  Target URL : %APP_URL%!RESET!
echo !DIM!  Tomcat     : %TOMCAT_HOME%!RESET!
echo !DIM!  Project    : %PROJECT_DIR%!RESET!
echo.

:: ============================================================
::  VERIFY PREREQUISITES
:: ============================================================
call :PrintStep "Verifying prerequisites..."

:: Check Java
"%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "JAVA_HOME is invalid or Java is not installed."
    call :PrintError "Expected: %JAVA_HOME%"
    goto :Fatal
)
for /F "tokens=3" %%v in ('"%JAVA_HOME%\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%v"
    goto :java_done
)
:java_done
call :PrintOK "Java found: !JAVA_VER!"

:: Check Maven
"%MAVEN_HOME%\bin\mvn.cmd" -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "MAVEN_HOME is invalid or Maven is not installed."
    call :PrintError "Expected: %MAVEN_HOME%"
    goto :Fatal
)
call :PrintOK "Maven found at: %MAVEN_HOME%\bin"

:: Check Tomcat directory
if not exist "%TOMCAT_HOME%\bin\startup.bat" (
    call :PrintError "Tomcat not found at: %TOMCAT_HOME%"
    call :PrintError "Please install Tomcat or update TOMCAT_HOME in this script."
    goto :Fatal
)
call :PrintOK "Tomcat found at: %TOMCAT_HOME%"

:: ============================================================
::  STEP 1 — MAVEN BUILD
:: ============================================================
echo.
call :PrintStep "STEP 1/4  —  Building project with Maven..."
echo !DIM!  Running: mvn clean package -DskipTests!RESET!
echo.

:: Navigate to project directory
cd /d "%PROJECT_DIR%"
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "Cannot navigate to project directory: %PROJECT_DIR%"
    goto :Fatal
)

:: Run Maven build
"%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "Maven build FAILED. Fix the errors above and retry."
    goto :Fatal
)

:: Confirm WAR was produced
if not exist "%PROJECT_DIR%target\%WAR_NAME%.war" (
    call :PrintError "Build succeeded but WAR file not found: target\%WAR_NAME%.war"
    goto :Fatal
)
call :PrintOK "Build successful  →  target\%WAR_NAME%.war"

:: ============================================================
::  STEP 2 — TOMCAT CHECK / START
:: ============================================================
echo.
call :PrintStep "STEP 2/4  —  Checking Tomcat status..."

set "TOMCAT_RUNNING=false"
tasklist /FI "IMAGENAME eq tomcat*" 2>nul | findstr /i "tomcat" >nul 2>&1
if %ERRORLEVEL% EQU 0 set "TOMCAT_RUNNING=true"

:: Also check via WMIC for java processes tied to catalina
if "!TOMCAT_RUNNING!"=="false" (
    wmic process where "commandline like '%%catalina%%'" get ProcessId 2>nul | findstr /r "[0-9]" >nul 2>&1
    if !ERRORLEVEL! EQU 0 set "TOMCAT_RUNNING=true"
)

if "!TOMCAT_RUNNING!"=="true" (
    call :PrintWarn "Tomcat is already running. Stopping it for a clean redeploy..."
    call "%TOMCAT_HOME%\bin\shutdown.bat" >nul 2>&1
    :: Give it time to shut down cleanly
    timeout /t 5 /nobreak >nul
    call :PrintOK "Tomcat stopped."
) else (
    call :PrintOK "Tomcat is not running — will start fresh."
)

:: ============================================================
::  STEP 3 — DEPLOY WAR
:: ============================================================
echo.
call :PrintStep "STEP 3/4  —  Deploying WAR to Tomcat..."

set "WEBAPPS=%TOMCAT_HOME%\webapps"

:: Remove old WAR file if present
if exist "%WEBAPPS%\%WAR_NAME%.war" (
    del /F /Q "%WEBAPPS%\%WAR_NAME%.war" >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        call :PrintError "Could not delete old WAR: %WEBAPPS%\%WAR_NAME%.war"
        goto :Fatal
    )
    call :PrintOK "Removed old WAR: webapps\%WAR_NAME%.war"
)

:: Remove old exploded context folder if present
if exist "%WEBAPPS%\%WAR_NAME%" (
    rd /S /Q "%WEBAPPS%\%WAR_NAME%" >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        call :PrintError "Could not remove exploded folder: %WEBAPPS%\%WAR_NAME%"
        goto :Fatal
    )
    call :PrintOK "Removed exploded folder: webapps\%WAR_NAME%\"
)

:: Copy new WAR to webapps
copy /Y "%PROJECT_DIR%target\%WAR_NAME%.war" "%WEBAPPS%\" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "Failed to copy WAR to Tomcat webapps directory."
    call :PrintError "Source : %PROJECT_DIR%target\%WAR_NAME%.war"
    call :PrintError "Dest   : %WEBAPPS%\"
    goto :Fatal
)
call :PrintOK "Deployed: webapps\%WAR_NAME%.war"

:: Start Tomcat
call :PrintOK "Starting Tomcat..."
start "Tomcat Server" "%TOMCAT_HOME%\bin\startup.bat"
if %ERRORLEVEL% NEQ 0 (
    call :PrintError "Failed to start Tomcat. Check %TOMCAT_HOME%\bin\startup.bat"
    goto :Fatal
)
call :PrintOK "Tomcat started successfully."

:: ============================================================
::  STEP 4 — WAIT AND LAUNCH BROWSER
:: ============================================================
echo.
call :PrintStep "STEP 4/4  —  Waiting for Tomcat to deploy context..."

:: Countdown timer (10 seconds)
for /L %%i in (10,-1,1) do (
    set /P "=!YELLOW!  Launching browser in %%i second(s)...  !RESET!" <nul
    echo.
    timeout /t 1 /nobreak >nul
)

echo.
call :PrintOK "Opening browser at %APP_URL%"
start "" "%APP_URL%"

:: ============================================================
::  DONE
:: ============================================================
echo.
echo !GREEN!!BOLD!  ╔══════════════════════════════════════════════════╗!RESET!
echo !GREEN!!BOLD!  ║          Deployment Complete!  ✓                  ║!RESET!
echo !GREEN!!BOLD!  ╚══════════════════════════════════════════════════╝!RESET!
echo.
echo !WHITE!  Application URL : !CYAN!%APP_URL%!RESET!
echo !WHITE!  Tomcat Logs     : !DIM!%TOMCAT_HOME%\logs\catalina.out!RESET!
echo.
echo !DIM!  Press any key to close this window...!RESET!
pause >nul
goto :EOF

:: ============================================================
::  FATAL ERROR — Stop and wait
:: ============================================================
:Fatal
echo.
echo !RED!!BOLD!  ╔══════════════════════════════════════════════════╗!RESET!
echo !RED!!BOLD!  ║   Deployment FAILED — See errors above.           ║!RESET!
echo !RED!!BOLD!  ╚══════════════════════════════════════════════════╝!RESET!
echo.
echo !DIM!  Press any key to close this window...!RESET!
pause >nul
exit /b 1

:: ============================================================
::  SUBROUTINES — Color-coded output helpers
:: ============================================================

:PrintOK
echo   !GREEN![ OK ]!RESET! %~1
goto :EOF

:PrintError
echo   !RED![FAIL]!RESET! %~1
goto :EOF

:PrintWarn
echo   !YELLOW![ ?? ]!RESET! %~1
goto :EOF

:PrintStep
echo !CYAN!!BOLD!━━━ %~1!RESET!
goto :EOF
