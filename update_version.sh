if [ "$1" == '' ]; then
	echo "update_version x.y.z"
	exit 1;
fi
./gradlew updateVersion -Pnewversion=%1