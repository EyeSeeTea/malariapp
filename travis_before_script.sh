openssl aes-256-cbc -K $encrypted_4558f4131c88_key -iv $encrypted_4558f4131c88_iv -in driveserviceprivatekey.json.enc -out driveserviceprivatekey.json -d
cp driveserviceprivatekey.json app/src/hnqis/res/raw/driveserviceprivatekey.json
cp driveserviceprivatekey.json app/src/eyeseetea/res/raw/driveserviceprivatekey.json
rm driveserviceprivatekey.json

