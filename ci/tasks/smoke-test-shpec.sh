#!/bin/bash

set -ex

url=""

if [ -z $APP_DOMAIN ]; then
  echo "APP_DOMAIN not set"
  exit 1
fi

if [ -z $APP_HOSTNAME ]; then
  echo "APP_HOSTNAME not set"
  exit 1
fi

url="http://${APP_HOSTNAME}.${APP_DOMAIN}"

echo "... updating apt-get and ensuring curl"
apt-get update && apt-get install -y curl

pushd git-src
  echo "Running smoke tests for App deployed at: $url"
  smoke-tests/bin/test $url
popd

exit 0
