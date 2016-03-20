#!/usr/bin/env bash
filePath="app/src/main/res/raw/lastcommit.txt"
echo "Last commit path $filePath"
commit=`git log -1 HEAD --format=%H`
echo "Saving last commit: $commit"
echo $commit > $filePath
echo "Done."
