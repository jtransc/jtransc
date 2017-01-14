@echo off
if "%1" == "" (
	echo "update_version x.y.z"
	exit /b
)
call gradlew updateVersion -Pnewversion=%1