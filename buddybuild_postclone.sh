#!/usr/bin/env bash
echo "Creating git post-checkout hook"
touch .git/hooks/post-checkout
echo "#!/usr/bash" >> .git/hooks/post-checkout
echo "echo \" running post-checkout hook \"" >> .git/hooks/post-checkout
echo "bash generate_last_commit.sh" >> .git/hooks/post-checkout
echo "ls" >> .git/hooks/post-checkout
echo "show hook post-checkout content"
cat .git/hooks/post-checkout
echo "reload actual branch"
git checkout

cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk
