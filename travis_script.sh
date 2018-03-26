#!/bin/bash
for variant in "$@"
do
	upperCaseVariant="${variant^}"
	echo "$upperCaseVariant"
	param="test${upperCaseVariant}DebugUnitTest"
	./gradlew --no-daemon $param
	param="test${upperCaseVariant}DebugTest"
	if [ $? != 0 ]
	then
		exit $?
	fi
	#Instrumental test:
	#./gradlew $param
done
