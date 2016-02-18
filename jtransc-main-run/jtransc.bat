@ECHO OFF
SET CWD_DIR=%CD%
SET POM_DIR=%~dp0
CALL mvn exec:java -f "%POM_DIR%/pom.xml" -Dexec.mainClass="com.jtransc.MainKt" -Dexec.workingdir="%CWD_DIR%" -q -Dexec.args="%*"
