#!/bin/sh

# Definitions
gitPath=$(git rev-parse --show-toplevel)
postCheckoutFile=${gitPath}/.git/hooks/post-checkout

# Generate last commit
sh ${gitPath}/generate_last_commit.sh

# Use the EST SDK branch
cd sdk
git checkout 2.25_EyeSeeTea
cd -
cp -a DBFlowORM sdk
cp ${BUDDYBUILD_SECURE_FILES}/driveserviceprivatekey.json app/src/hnqis/res/raw/driveserviceprivatekey.json

