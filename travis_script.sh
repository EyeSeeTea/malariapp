#!bin/bash
for var in "$@"
do
   ./gradlew test"$var"DebugUnitTest --stacktrace
done
