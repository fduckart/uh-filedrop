UH FileDrop web application.

[![Build Status](https://travis-ci.org/fduckart/uh-filedrop.png?branch=master)](https://travis-ci.org/fduckart/uh-filedrop)
[![Coverage Status](https://coveralls.io/repos/github/fduckart/uh-filedrop/badge.svg)](https://coveralls.io/github/fduckart/uh-filedrop)
##### Java
You'll need a Java JDK to build and run the project (version 1.8).

##### Building
Install the necessary project dependencies from the command line:

    $ ./mvnw install

To start the application:

    $ ./mvnw clean spring-boot:run

After the application starts, navigate to here in a web browser:

<http://localhost:8080/filedrop>

##### Running Unit Tests
The project includes Unit Tests for various parts of the system.
For this project, Unit Tests are defined as those tests that will
rely on only the local development computer.
A development build of the application will run the Unit Tests.

To run the unit tests:

    $ ./mvnw clean test

To run a test single test class:

    $ ./mvnw clean test -Dtest=StringsTest

To run a single method in a test class:

    $ ./mvnw clean test -Dtest=StringsTest#trunctate

##### Build to deploy to an Environment
To build a deployable war file for deployment:

    $ ./mvnw clean package

You should have a deployable war file in the target directory.
Deploy as usual in a servlet container, e.g. tomcat.

_Important Note:_
If you are setting up tomcat for the first time,
make sure you enable SSL and add any necessary certificates.

Here are instructions for Tomcat 8, for example:
<https://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html>

Copy the filedrop.war file into the webapps directory of Tomcat.

##### Build Tool (Optional)
Download and install maven (version 3.2.1+).

Be sure to set up a M2_REPO environment variable.

##### Source Repository
The files for the project are kept here:

<https://github.com/fduckart/uh-filedrop>

##### Important Note
The UH Number is restricted by University of Hawaii policy, so be sure not to expose it on any public page.
