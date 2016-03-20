#!/usr/bin/env bash
cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk

echo "Creating git post-checkout hook"
echo "#!/usr/bin/env bash" >> .git/hooks/post-checkout
echo "echo \" running post-checkout hook \"" >> .git/hooks/post-checkout
echo "bash generate_last_commit.sh" >> .git/hooks/post-checkout
echo "reload actual branch"
echo "git checkout"
git checkout -- buddybuild_postclone.sh