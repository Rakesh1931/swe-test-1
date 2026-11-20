@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
cd /d "%~dp0backend"
call gradlew.bat bootRun
