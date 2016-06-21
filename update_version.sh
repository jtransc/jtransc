if [ "$1" == '' ]; then
	echo "update_version x.y.z"
	exit 1;
fi

sed -i -e "s/static private final String version = \"\(.*\)\";/static private final String version = \"$1\";/g" jtransc-rt-core/src/com/jtransc/JTranscVersion.java
sed -i -e "s/jtranscVersion=\(.*\)/jtranscVersion=$1/g" gradle.properties
sed -i -e "s/jtranscVersion=\(.*\)/jtranscVersion=$1/g" jtransc-main-run/example-gradle/gradle.properties
sed -i -e "s/jtranscVersion=\(.*\)/jtranscVersion=$1/g" benchmark/gradle.properties
sed -i -e "s/<!--jtransc--><version>\(.*\)<\/version>/<!--jtransc--><version>$1<\/version>/g" jtransc-main-run/pom.xml
sed -i -e "s/<!--jtransc--><version>\(.*\)<\/version>/<!--jtransc--><version>$1<\/version>/g" jtransc-maven-plugin/example/pom.xml

