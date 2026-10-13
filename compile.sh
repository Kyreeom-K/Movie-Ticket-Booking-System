#!/bin/bash
# Compiles everything reachable from ui.Main into the "out" folder (Linux / macOS)
mkdir -p out
javac -encoding UTF-8 -sourcepath src -d out src/ui/Main.java && echo "Compiled successfully."
