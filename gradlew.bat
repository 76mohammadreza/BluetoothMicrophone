@echo off
setlocal
set "PROJECT_DIR=%~dp0"
set "GRADLE_VER=8.11.1"
set "DIST=%USERPROFILE%\.gradle\manual-distributions\gradle-%GRADLE_VER%"
if not exist "%DIST%in\gradle.bat" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $base=Join-Path $env:USERPROFILE '.gradle/manual-distributions'; New-Item -ItemType Directory -Force -Path $base | Out-Null; $zip=Join-Path $env:TEMP 'gradle-8.11.1-bin.zip'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip' -OutFile $zip; Expand-Archive -Path $zip -DestinationPath $base -Force; Remove-Item $zip -Force"
  if errorlevel 1 exit /b 1
)
cd /d "%PROJECT_DIR%"
call "%DIST%in\gradle.bat" %*
