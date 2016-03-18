#!/usr/bin/env bash
filePath="app/src/main/res/raw/commit.txt"
echo "execute commit command in path $filePath"
commit=`git log -1 HEAD --format=%H`
echo $commit
cat $filePath
echo $commit > $filePath
cat $filePath

cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk
