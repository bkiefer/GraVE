#!/bin/bash
#set -x
script_dir=$(dirname $(realpath $0))
here=`pwd`
cd "$script_dir"

# check out and update all modules
git pull --recurse-submodules
git submodule update --init --recursive --remote
cd compiler/vonda; mvn install -DskipTests; cd ../..
export PATH="$PATH:`pwd`/compiler/vonda/bin"
#echo $PATH
# pass -n to skip building test projects
if test "$1" = "-n"; then
    mvn -pl model,compiler,editor -am install
else
    compiler/vonda/compiler/src/test/resources/ontologies/vonda/ntcreate.sh
    mvn install
fi
