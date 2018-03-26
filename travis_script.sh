#!/bin/bash
for variant in "$@"
do
	upperCaseVariant="${variant^}"
	echo "$upperCaseVariant"
	param="test${upperCaseVariant}DebugUnitTest"
	$command='./gradlew --no-daemon $param'
	if [ eval $command != 0 ]
	then
		exit 1
	fi
done
