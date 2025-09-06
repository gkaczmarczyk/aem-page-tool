@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"

set "JAR_PATH=%SCRIPT_DIR%aem-page-tool.jar"

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Java executable not found. Please install Java and ensure it's in your PATH.
    exit /b 1
)

set JAVA_OPTS=-Xms256m -Xmx1g

java %JAVA_OPTS% -jar "%JAR_PATH%" %*

if %ERRORLEVEL% neq 0 (
    echo Error: Execution failed with exit code %ERRORLEVEL%.
    exit /b 1
)

endlocal
