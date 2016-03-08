#!/usr/bin/env bash

filePath="app\src\main\res\raw\commit.txt"
commit=git log  -1 --pretty=format:"%h"
echo $commit>$filePath

cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk
