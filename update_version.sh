if [ "$1" == '' ]; then
	echo "update_version x.y.z"
	exit 1;
fi

sed -i -e "s/static private final String version = \"\(.*\)\";/static private final String version = \"$1\";/g" jtransc-rt-core/src/com/jtransc/JTranscVersion.java
mvn versions:set -DnewVersion=$1