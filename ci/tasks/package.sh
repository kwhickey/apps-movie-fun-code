#!/bin/bash

set -ex

sourcedir=git-src
outputdir=package-output
jarname=moviefun.war

echo "VARIABLES: 
  sourcedir=$sourcedir 
  outputdir=$outputdir
  jarname=$jarname"

pushd $sourcedir
  echo "Packaging WAR"
  ./mvnw --batch-mode clean package -DskipTests -Dmaven.test.skip=true
popd

jar_count=`find $sourcedir/target -type f -name *.war | wc -l`

echo "Found $jar_count WAR file(s) at $sourcedir/target"

if [ $jar_count -gt 1 ]; then
  echo "More than one war found at $sourcedir/target, don't know which one to deploy. Exiting"
  exit 1
fi

if [ $jar_count -eq 0 ]; then
  echo "No war found at $sourcedir/target. Ensure it was built. Exiting"
  exit 1
fi

echo "Copying war from $sourcedir/target to $outputdir/$jarname, from working directory: $(pwd)"
if [ ! -d "$outputdir" ]; then
  echo "Directory $outputdir does not exist! Cannot copy output to it. Exiting"
  exit 1
fi
find $sourcedir/target -type f -name *.war -exec cp "{}" $outputdir/$jarname \;

echo "Done packaging"
exit 0
