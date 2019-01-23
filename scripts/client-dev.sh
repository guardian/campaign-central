#!/usr/bin/env bash

printf "\n\rStarting Webpack Dev Server... \n\r\n\r"
yarn run client-dev &
clientDevPid=$!

printf "\n\rStarting Play App... \n\r\n\r"
AWS_PROFILE=composer JS_ASSET_HOST=https://campaign-central-assets.local.dev-gutools.co.uk/assets/ sbt run

printf "\n\rKilling Webpack Dev Server... \n\r\n\r"
kill $clientDevPid
