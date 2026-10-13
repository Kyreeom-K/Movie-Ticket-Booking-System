@echo off
REM Compiles everything reachable from ui.Main into the "out" folder (Windows).
REM Works even when the project path contains spaces (e.g. a Desktop folder).
if not exist out mkdir out
javac -encoding UTF-8 -sourcepath src -d out src\ui\Main.java && echo Compiled successfully.
