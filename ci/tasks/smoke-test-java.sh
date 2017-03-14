#!/bin/bash

set -ex

if [ -z $APP_DOMAIN ]; then
  echo "APP_DOMAIN not set"
  exit 1
fi

if [ -z $APP_HOSTNAME ]; then
  echo "APP_HOSTNAME not set"
  exit 1
fi

export MOVIE_FUN_URL="http://${APP_HOSTNAME}.${APP_DOMAIN}"

pushd git-src
  echo "Running java smoke tests for App deployed at: $MOVIE_FUN_URL"
  ./mvnw --batch-mode test
popd

exit 0
