@ECHO OFF
SET BASEDIR=%~dp0
SET CWD=%CD%
CALL mvn -q -f "%BASEDIR%/pom.xml" exec:java -Dexec.workingdir="%CWD%" -Dexec.args="%*"
