@echo off
setlocal EnableDelayedExpansion

:: --- CONFIGURATION ---
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "MAVEN_HOME=C:\Program Files\NetBeans-24\netbeans\java\maven"
set "TOMCAT_HOME=C:\xampp\tomcat"
set "PROJECT_NAME=OceanViewResort"
set "PROJECT_DIR=%~dp0%PROJECT_NAME%"

:: පථයන් පද්ධතියට එක් කිරීම
set "PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%"

cls
color 0B
echo ============================================================
echo      OCEAN VIEW RESORT - SYSTEM LAUNCHER (STABLE)
echo ============================================================

:: පියවර 1: Maven Build
echo [1] Running Maven Build...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    color 0C
    echo [FAIL] Maven Build එක අසාර්ථකයි! කරුණාකර Java code එක පරීක්ෂා කරන්න.
    goto :ENDER
)
echo [OK] Build Successful!
echo ------------------------------------------------------------

:: පියවර 2: Tomcat පරීක්ෂාව
echo [2] Checking Tomcat at "%TOMCAT_HOME%"...
if not exist "%TOMCAT_HOME%\bin\startup.bat" (
    color 0C
    echo [FAIL] Tomcat සොයාගත නොහැක! 
    echo ඔයාගේ Path එක වැරදියි: "%TOMCAT_HOME%"
    echo කරුණාකර 'C:\xampp\tomcat\bin' ඇතුළේ 'startup.bat' තියෙනවද බලන්න.
    goto :ENDER
)
echo [OK] Tomcat Found.
echo ------------------------------------------------------------

:: පියවර 3: පරණ ගොනු ඉවත් කිරීම
echo [3] Cleaning webapps folder...
if exist "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war" del /f /q "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war"
if exist "%TOMCAT_HOME%\webapps\%PROJECT_NAME%" rd /s /q "%TOMCAT_HOME%\webapps\%PROJECT_NAME%"
echo [OK] Cleaned.
echo ------------------------------------------------------------

:: පියවර 4: WAR එක Copy කිරීම
echo [4] Copying fresh WAR to Tomcat...
copy /y "%PROJECT_DIR%\target\%PROJECT_NAME%.war" "%TOMCAT_HOME%\webapps\"
if %ERRORLEVEL% neq 0 (
    color 0C
    echo [FAIL] File එක copy කරන්න බැරි වුණා. Tomcat දැනටමත් රන් වෙනවාද?
    goto :ENDER
)
echo [OK] WAR copied successfully.
echo ------------------------------------------------------------

:: පියවර 5: Tomcat සහ Browser එක පණගැන්වීම
echo [5] Starting Tomcat Server...
cd /d "%TOMCAT_HOME%\bin"
:: 'call' වෙනුවට 'start' භාවිතා කර වෙනම window එකක් ගනිමු
start "TOMCAT_SERVER" cmd /c startup.bat

echo.
echo ============================================================
echo   SUCCESS: System is deploying!
echo   Launching Browser in 10 seconds...
echo ============================================================
timeout /t 10
start "" "http://localhost:8080/%PROJECT_NAME%"

:ENDER
echo.
echo Press ANY key to close this window.
pause > nul