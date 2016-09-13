#!/bin/sh

# Definitions
gitPath=$(git rev-parse --show-toplevel)
postCheckoutFile=${gitPath}/.git/hooks/post-checkout

# Generate last commit
sh ${gitPath}/generate_last_commit.sh

# Use the EST SDK branch
cd sdk
git checkout 2.22_EyeSeeTea
cd -
cd DBFlowORM
git checkout update_to_gradle_2.0
cd -
cp -a DBFlowORM sdk

