<h1 align="center">
  <a href="https://github.com/orangebeard-io/cucumber-java-listener">
    <img src="https://raw.githubusercontent.com/orangebeard-io/cucumber-java-listener/master/.github/logo.svg" alt="Orangebeard.io Cucumber Java Listener" height="200">
  </a>
  <br>Orangebeard.io Cucumber Java Listener<br>
</h1>

<h4 align="center">Orangebeard listener for the <a href="https://cucumber.io/" target="_blank" rel="noopener">Cucumber</a> Java Behavior-Driven-Development framework.</h4>

<p align="center">
  <a href="https://repo.maven.apache.org/maven2/io/orangebeard/cucumber-java-listener/">
    <img src="https://img.shields.io/maven-central/v/io.orangebeard/cucumber-java-listener?style=flat-square"
      alt="MVN Version" />
  </a>
  <a href="https://github.com/orangebeard-io/cucumber-java-listener/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/orangebeard-io/cucumber-java-listener/release.yml?branch=main&style=flat-square"
      alt="Build Status" />
  </a>
  <a href="https://github.com/orangebeard-io/cucumber-java-listener/blob/master/LICENSE.txt">
    <img src="https://img.shields.io/github/license/orangebeard-io/cucumber-java-listener?style=flat-square"
      alt="License" />
  </a>
</p>

<div align="center">
  <h4>
    <a href="https://orangebeard.io">Orangebeard</a> |
    <a href="#installation">Installation</a> |
    <a href="#configuration">Configuration</a>
  </h4>
</div>

## Installation

### Install the mvn package

Add the dependency to your pom:
```xml
<dependency>
    <groupId>io.orangebeard</groupId>
    <artifactId>cucumber-java-listener</artifactId>
    <version>version</version>
    <scope>test</scope>
</dependency>
```

## Configuration
Make sure your Cucumber setup makes use of <a href="https://cucumber.io/docs/cucumber/api/#junit">JUnit</a> to execute scenarios. 

Add the following plugin to the list of your plugins (comma separated) to your `junit-platform.properties` in the test resources folder:

```properties
cucumber.plugin=io.orangebeard.listener.OrangebeardCucumberListener
```

Create a new file named `orangebeard.properties` in the test resources folder. Add the following properties:

```properties
orangebeard.endpoint=<ORANGEBEARD-ENDPOINT>
orangebeard.accessToken=<XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX>
orangebeard.project=<PROJECT_NAME>
orangebeard.testset=<TESTSET_NAME>

# optional
orangebeard.description=<DESCRIPTION>
orangebeard.attributes=key:value; value;
```
For more configuration options, see: https://docs.orangebeard.io/connecting-test-tools/listener-api-clients/#auto-config


### Environment variables

The properties above can be set as environment variables as well. Environment variables will override property values. In the environment variables, it is allowed to replace the dot by an underscore.
for example: ```orangebeard_endpoint``` as an environment variable will work as well.
