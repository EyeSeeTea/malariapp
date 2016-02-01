#!/usr/bin/env bash

rm -rf DBFlowORM
git clone git@github.com:EyeSeeTea/DBFlow.git DBFlowORM
rm -rf sdk
git clone -b 2.22_EyeSeeTea git@github.com:EyeSeeTea/dhis2-android-sdk.git sdk
