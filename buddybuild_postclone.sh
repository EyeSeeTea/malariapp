#!/usr/bin/env bash
echo "#!/usr/bin/env bash" >> .git/hooks/post-checkout
echo "bash generate_last_commit.sh" >> .git/hooks/post-checkout
git checkout
cd sdk
git checkout 2.22_EyeSeeTea
cd -
cp -a DBFlowORM sdk
