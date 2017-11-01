#!/bin/sh

# Definitions
gitPath=$(git rev-parse --show-toplevel)
postCheckoutFile=${gitPath}/.git/hooks/post-checkout

# Generate last commit
sh ${gitPath}/generate_last_commit.sh

# Use the EST SDK branch
cd sdk
git checkout v1.2_hnqis
cd -
cp ${BUDDYBUILD_SECURE_FILES}/driveserviceprivatekey.json app/src/hnqis/res/raw/driveserviceprivatekey.json

