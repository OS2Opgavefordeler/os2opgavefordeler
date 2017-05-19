Getting Started
===============
This file explains the steps to get started with working on this project.

Development Backend
-------------------
The project builds with Java 8. Make sure to enable this first.

This project provides a Wildfly 9, and a bit of configuration, from a Docker based setup. Build the Docker image from the Dockerfile in project root. Instructions for build, run and configuration is provided there.
Make sure to run the docker exec... to enable God Mode login in dev.
 
Database setup is H2. Check the persistence.xml to enable create-drop and dialects for dev and make sure that test and prod deployments do NOT create drop!

Bootstrap data is configurable in BootstrappingDataProviderSingleton, which can be triggered from a dev env login page. This page also provides an easy God Mode login.

Development Frontend
--------------------
Frontend is AngularJS based and built with Gulp.

Make sure you install dependencies with both NPM and bower.

When all set up, run

`gulp watch-dev` 

to get a dev server running on port 9001 localhost.


Detailed Step By Step - Backend
-----
Checkout the develop branch from git repository https://github.com/OS2Opgavefordeler/os2opgavefordeler

Compile the project with maven using the dev profile. The dev profile sets up H2 as the hibernate datasource.
mvn clean install -Denv=dev

Build the docker image and tag the image "os2opgavefordeler"
docker build -t os2opgavefordeler .

Run os2opgavefordeler docker image. Name it "os2opgavefordeler" and map container ports to host ports.
docker run -p 8080:8080 -p 9990:9990 --name os2opgavefordeler os2opgavefordeler

Verify Wildfly start page:
http://localhost:8080

Verify Wildfly Management Console:
http://localhost:9990
User: admin
Pass: admin

Verify TopicRouter healthcheck:
http://localhost:8080/TopicRouter/rest/api/healthcheck
Should output "We get signal."

Configure system properties (enable godmode, disable tracelogging, set client id and set client secret)
docker cp ./environment/docker/commands.cli os2opgavefordeler:/tmp/commands.cli
docker exec os2opgavefordeler wildfly/bin/jboss-cli.sh -c --file=/tmp/commands.cli

Bootstrap test data using BootstrappingDataProviderSingleton bean
http://localhost:8080/TopicRouter/rest/distributionrulefilter/bootstrap

Login with godmode (use a valid email from the BootstrappingDataProviderSingleton bean):
http://localhost:8080/TopicRouter/rest/auth/iddqd?email=hlo@miracle.dk
If godmode login succeeds you are logged in and redirected to localhost:9001 (topicrouter.url.home default)
If godmode login fails you get an internal server error

Detailed Step By Step - Frontend
------
install node from http://nodejs.org/
 
install dependencies:
npm install -g bower
npm install -g gulp
npm install
 
When all set up, run
gulp watch-dev
to get a dev server running on port 9001 localhost.
 
Verify working frontend
http://localhost:9001

How To Query Against H2
------

