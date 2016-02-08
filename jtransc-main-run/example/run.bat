@echo off
javac HelloWorld.java
call ../all.bat . -main HelloWorld -release -out program.swf -run
