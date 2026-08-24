@echo off
setlocal
set APP_HOME=%~dp0
set BOOTSTRAP_DIR=%APP_HOME%.gradle-bootstrap
set WRAPPER_JAR=%BOOTSTRAP_DIR%\gradle-wrapper.jar
set WRAPPER_SOURCE=%APP_HOME%gradle\wrapper\gradle-wrapper.jar.base64
set WRAPPER_PROPERTIES=%BOOTSTRAP_DIR%\gradle-wrapper.properties
set WRAPPER_PROPERTIES_SOURCE=%APP_HOME%gradle\wrapper\gradle-wrapper.properties

if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
if not exist "%WRAPPER_JAR%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "[IO.File]::WriteAllBytes('%WRAPPER_JAR%', [Convert]::FromBase64String((Get-Content -Raw '%WRAPPER_SOURCE%')))"
  if errorlevel 1 exit /b 1
)
copy /Y "%WRAPPER_PROPERTIES_SOURCE%" "%WRAPPER_PROPERTIES%" >nul

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
