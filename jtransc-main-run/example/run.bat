@echo off
javac HelloWorld.java
call ../jtransc.bat . -main HelloWorld -release -out program.js -run
