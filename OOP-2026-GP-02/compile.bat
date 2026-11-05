@echo off
dir /s /b src\*.java > sources.txt
javac -cp "lib/*;src" -sourcepath src -d out @sources.txt
echo Compilation finished with exit code %ERRORLEVEL%

