@echo off
title Mini Addon Loader - Minecraft 26.2
echo ==========================================
echo Mini Addon Loader - Minecraft 26.2
echo ==========================================
echo.
java -version
if errorlevel 1 (
  echo [ERROR] Java tidak ditemukan.
  echo Minecraft 26.2 membutuhkan Java 25.
  pause
  exit /b 1
)
if not exist gradlew.bat (
  echo [ERROR] gradlew.bat tidak ditemukan.
  echo Buat/download Gradle Wrapper terlebih dahulu.
  pause
  exit /b 1
)
call gradlew.bat clean build
if errorlevel 1 (
  echo.
  echo BUILD FAILED.
  pause
  exit /b 1
)
echo.
echo BUILD SUCCESSFUL!
echo Core: core\build\libs\
echo Addon: example-addon\build\libs\
pause
