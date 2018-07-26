# Teamcity build configuration

## VCS

Default branch: `refs/heads/master`  
Branch specification: `+:refs/heads/*`

## Build steps

1. Command line custom script running in `node:latest` Docker container:
```
#!/bin/bash

set -e -x

yarn install
yarn run build
yarn run build-icons
```
Following instructions [here](https://blog.jetbrains.com/teamcity/2017/08/build-react-apps-with-teamcity/).

2. SBT `clean compile test riffRaffUpload`

## Build features

Commit status publisher: access token auto-generated.
