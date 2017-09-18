Campaign Central
================

Install and Setup
=================

### nginx set up

The Nginx setup uses the [dev-nginx](https://github.com/guardian/dev-nginx) tool.

In the `http { }` block of your nginx config file `/usr/local/etc/nginx/nginx.conf` make sure that the two following lines are present

```
include sites-enabled/*;
server_names_hash_bucket_size 256;
```

### configuration

Before you run for the first time you will need to run `scripts/setup.sh`.  This will install and compile all the frontend dependencies needed for the app (you may need to install npm before you can run this successfully). If any frontend dependencies are changed you should should re-run the setup script.

You will then need to create a local configuration file. The easiest way to do this is to copy the example config:  

```
cp conf/example-local.conf conf/local.conf
```

The campaign-central is a standard play app, so to run it's `sbt run`.  You will need to have developer credentials configured in an AWS composer profile to be able to run it.  To test that the play app is running independently of nginx, try [hitting http://localhost:2267](hitting http://localhost:2267).

After running this, campaign central
will be available at [https://campaign-central.local.dev-gutools.co.uk](https://campaign-central.local.dev-gutools.co.uk).

By default if you change any frontend code, you will need to recompile the assets using `scripts/setup.sh` but there are alternatives:

Client Side Development
=======================

We use webpack to compile the assets for this project. You have the option to run `scripts/setup.sh` after each change as mentioned above, or alternatively you can choose to use one of the alternative startup scripts provided

1. `scripts/start.sh` This starts a webpack watcher in addition to running the application - The watcher will compile unminified code when it detects a change to the javascript. Refresh the webpage to see the new code.

2. `scripts/client-dev.sh` This scripts starts a [webpack-dev-server](https://webpack.github.io/docs/webpack-dev-server.html)
alongside the application which provides Hot Reloading. Changes you make to code should be immediately visible in the browser

Server Side Development
=======================

The backend code used the standard scala play layout.

The frontend components live in the public directory in root. Css is compiled from sass file in the style directory.
