#!/bin/bash
echo "start travis before script"
openssl aes-256-cbc -K $encrypted_b4da38d765e0_key -iv $encrypted_b4da38d765e0_iv -in app.tar.enc -out app.tar -d
echo "decrypted file"
tar -xvzf app.tar
echo "continue"
