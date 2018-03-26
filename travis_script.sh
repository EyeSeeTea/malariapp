#!/bin/bash
for variant in "$@"
do
	upperCaseVariant="${variant^}"
	echo "$upperCaseVariant"
	param="test${upperCaseVariant}DebugUnitTest"
	commandResult=`./gradlew --no-daemon $param`


	if [ $commandResult != "0" ]
	then
		exit 1
	fi
done
