#!/bin/bash
echo "start travis before script"
openssl aes-256-cbc -K $encrypted_77edd05762cb_key -iv $encrypted_77edd05762cb_iv -in app.tar.enc -out app.tar -d
echo "decrypted file"
tar -xvzf app.tar
echo no | android create avd --force -n test -t android-25 --abi armeabi-v7a
emulator -avd test -no-audio -no-window &
android-wait-for-emulator
adb devices
adb shell input keyevent 82
echo "continue"
