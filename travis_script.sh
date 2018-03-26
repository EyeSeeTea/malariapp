#!/bin/bash
for variant in "$@"
do
	upperCaseVariant="${variant^}"
	echo "$upperCaseVariant"
	param = "test" + $upperCaseVariant + "DebugUnitTest"
	./gradlew $param -i
done
