#!/bin/bash
echo "start travis before script"
openssl aes-256-cbc -K $encrypted_77edd05762cb_key -iv $encrypted_77edd05762cb_iv -in app.tar.enc -out app.tar -d
echo "decrypted file"
tar -xvzf app.tar
echo "continue"
