#!/usr/bin/env bash

printf "\n\r 🙏  Setting up Campaign Central Client Side dependencies... \n\r\n\r"
printf "\n\r 📦  Installing NPM packages... \n\r\n\r"

yarn install

printf "\n\r ⏳  Compiling JavaScript... \n\r\n\r"

yarn run build

printf "\n\r 🛠  Building Icons... \n\r\n\r"

yarn run build-icons

printf "\n\r 👍  Done.\n\r\n\r"
