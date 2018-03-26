#!/bin/bash
for variant in "$@"
do
	upperCaseVariant="${variant^}"
	echo "$upperCaseVariant"
	param="test${upperCaseVariant}DebugUnitTest"
	./gradlew $param
	param="test${upperCaseVariant}DebugTest"
	#Instrumental test:
	#./gradlew $param
done
