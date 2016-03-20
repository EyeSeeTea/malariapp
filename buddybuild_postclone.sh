#!/usr/bin/env bash
echo "Creating git post-checkout hook"
echo "#!/usr/bin/env bash" >> .git/hooks/post-checkout
echo "echo \" running post-checkout hook \"" >> .git/hooks/post-checkout
echo "bash generate_last_commit.sh" >> .git/hooks/post-checkout
echo "reload actual branch"
branch=`git rev-parse --abbrev-ref HEAD`
git stash
git checkout development
git stash
git checkout $branch



cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk
