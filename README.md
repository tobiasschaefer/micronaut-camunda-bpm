# micronaut-camunda-bpm
**Integration between Micronaut and Camunda BPM Process Engine**

This project allows you to easily integrate the [Camunda BPM Process Engine](https://camunda.com/products/bpmn-engine/) into existing [Micronaut](https://micronaut.io) projects.

We configure Camunda BPM with sensible defaults, so that you can get started with minimum configuration: simply add a dependency in your Micronaut project!

Advantages of Micronaut together with Camunda BPM:
* Monumental leap in startup time (Currently still blocked by [micronaut-core#2867](https://github.com/micronaut-projects/micronaut-core/issues/2867)) and MyBatis initialization.
* Minimal memory footprint
* (...)

Advantages of Camunda BPM together with Micronaut:
* BPMN 2.0 Support
* Embedded process engine with low memory footprint
* (...)

Do you want to contribute to our open source project? Please read the [Contribution Guidelines](CONTRIBUTING.md) and [contact us](#contact).

Micronaut + Camunda BPM = :heart:

[![Release](https://img.shields.io/github/v/release/NovatecConsulting/micronaut-camunda-bpm.svg)](https://github.com/NovatecConsulting/micronaut-camunda-bpm/releases)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Continuous Integration](https://github.com/NovatecConsulting/micronaut-camunda-bpm/workflows/Continuous%20Integration/badge.svg)](https://github.com/NovatecConsulting/micronaut-camunda-bpm/actions)
[![Join the chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/NovatecConsulting/micronaut-camunda-bpm)

# Features
* Camunda BPM can be integrated into a Micronaut project by simply adding a dependency in build.gradle (Gradle) or pom.xml (Maven).
* Using h2 as an in-memory database is as simple as adding a dependency. Other data sources can be configured via properties.
* The Camunda process engine with its job executor is started automatically.
* Models (*.bpmn, *.cmmn, and *.dmn) found in the root of the resources are automatically deployed.
* The process engine and related services, e.g. RuntimeService, RepositoryService, ..., are provided as lazy initialized beans and can be injected.
* Micronaut beans are resolved from the application context if they are referenced by expressions or Java class names within the process models.
* The process engine configuration can be customized programmatically.
* The process engine integrates with Micronaut's transaction manager. Optionally, micronaut-data-jdbc or micronaut-data-jpa are supported.

# Getting Started

This section describes what needs to be done to use `micronaut-camunda-bpm-feature` in a Micronaut project.

We officially support the JDK LTS releases 8 and 11 and the latest release supported by Micronaut (currently JDK 15).

Do you need an example? See our example application at [/micronaut-camunda-bpm-example](/micronaut-camunda-bpm-example). 

## Add Dependency using Gradle
1. (Optionally) create an empty Micronaut project with `mn create-app my-example` or use [Micronaut Launch](https://launch.micronaut.io).
2. Add the dependency in build.gradle:
```groovy
implementation("info.novatec:micronaut-camunda-bpm-feature:0.7.0")
runtimeOnly("com.h2database:h2")
```

Note: The module `micronaut-camunda-bpm-feature` includes the dependency `org.camunda.bpm:camunda-engine` which will be resolved transitively.

## Add Dependency using Maven
1. (Optionally) create an empty Micronaut project with `mn create-app my-example --build=maven` or use [Micronaut Launch](https://launch.micronaut.io).
2. Add the dependency in pom.xml:
```xml
<dependency>
  <groupId>info.novatec</groupId>
  <artifactId>micronaut-camunda-bpm-feature</artifactId>
  <version>0.7.0</version>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

Note: The module `micronaut-camunda-bpm-feature` includes the dependency `org.camunda.bpm:camunda-engine` which will be resolved transitively.

##  Deploying process models
To deploy a process model create an executable BPMN file and save it in the resources' root. When starting the application you'll see the logs saying:

`Deploying model: helloworld.bpmn`

## Calling Camunda BPM Process Engine and related Services

Inject the process engine or any related services using constructor injection:
```java
// ...

import javax.inject.Singleton;

@Singleton
public class MyComponent {

    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    
    public MyComponent(ProcessEngine processEngine, RuntimeService runtimeService) {
        this.processEngine = processEngine;
        this.runtimeService = runtimeService;
    }

    // ...
}
```

Alternatively to constructor injection, you can also use field injection, JavaBean property injection, or method parameter injection.

## Invoking Java Delegates

To invoke a Java Delegate first create a singleton bean:

```java
@Singleton
public class LoggerDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LoggerDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("Hello World: {}", delegateExecution);
    }
}
```

and then reference it the process model with the expression`${loggerDelegate}`.

## Configuration

### Data Source

By default, an in-memory H2 data source will be used. Remember to add the runtime dependency `com.h2database:h2` mentioned in [Getting Started](#getting-started).

However, you can configure any other database, e.g. in `application.yml`:

```yaml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: secret
    driverClassName: org.postgresql.Driver
```

after adding the appropriate driver as a dependency:

```groovy
runtimeOnly "org.postgresql:postgresql:42.2.18"
```

### Properties

You may use the following properties (typically in application.yml) to configure the Camunda BPM integration.

| Prefix               |Property          | Default                                      | Description            |
|----------------------|------------------|----------------------------------------------|------------------------|
| camunda.bpm          | .history-level   | auto                                         | Camunda history level, use one of [`full`, `audit`, `activity`, `none`, `auto`]. `auto` uses the level already present in the database, defaulting to `full`. |
| camunda.bpm.database | .schema-update   | true                                         | If automatic schema update should be applied, use one of [`true`, `false`, `create`, `create-drop`, `drop-create`] |
| camunda.bpm.telemetry| .enabled         | false                                        | Enable to periodically report anonymized telemetry data to Camunda, i.e. letting them know you're using this integration project. |

### Custom Process Engine Configuration

Internally, to build Camunda `ProcessEngine` we use `ProcessEngineConfiguration`. This process can be intercepted for detailed configuration customization with the following bean:

```java
@Singleton
@Replaces(DefaultProcessEngineConfigurationCustomizer.class)
public class MyProcessEngineConfigurationCustomizer implements ProcessEngineConfigurationCustomizer  {

    @Override
    public void customize(ProcessEngineConfiguration configuration) {
        // configure ProcessEngineConfiguration here, e.g.:
        configuration.setProcessEngineName("CustomizedEngine");
    }

}
```

## Using micronaut-data-jdbc or micronaut-data-jpa

The process engine integrates with Micronaut's transaction manager and uses Hikari Connection Pool:
* When interacting with the process engine, e.g. starting or continuing a process, the existing transaction will be propagated.
* JavaDelegates and Listeners will have the surrounding Camunda transaction propagated to them allowing the atomic persistence of data.

Optionally, micronaut-data-jdbc or micronaut-data-jpa are supported.

### Alternative 1: micronaut-data-jdbc

To enable embedded transactions management support **with micronaut-data-jdbc** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-data-jdbc")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-jdbc</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure the JDBC properties as described [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#jdbc).

### Alternative 2: micronaut-data-jpa

To enable embedded transactions management support **with micronaut-data-jpa** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-hibernate-jpa")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-hibernate-jpa</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure JPA as described in [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#hibernate).

## Compatibility Matrix

The following compatibility matrix shows the officially supported Micronaut and Camunda BPM versions for each release.
Other combinations might also work but have not been tested.  

| Release |Micronaut | Camunda BPM |
|-------|-------|--------|
| 0.7.0 | 2.1.1 | 7.13.0 |
| 0.6.0 | 2.1.0 | 7.13.0 |
| 0.5.3 | 2.0.1 | 7.13.0 |
| 0.5.2 | 2.0.0 | 7.13.0 |
| 0.5.1 | 2.0.0 | 7.13.0 |
| 0.5.0 | 2.0.0 | 7.13.0 |
| 0.4.2 | 1.3.6 | 7.13.0 |
| 0.3.1 | 1.3.5 | 7.12.0 |
| 0.2.2 | 1.3.3 | 7.12.0 |
| 0.2.1 | 1.3.3 | 7.12.0 |
| 0.2.0 | 1.3.3 | 7.12.0 |
| 0.1.0 | 1.3.3 | 7.12.0 |

Download of Releases:
* [GitHub Artifacts](https://github.com/NovatecConsulting/micronaut-camunda-bpm/releases)
* [Maven Central Artifacts](https://search.maven.org/artifact/info.novatec/micronaut-camunda-bpm-feature)

## Contact

This open source project is being developed by [Novatec Consulting GmbH](https://www.novatec-gmbh.de/en/) with the support of the open source community.

If you have any questions or ideas feel free to create an [issue](https://github.com/NovatecConsulting/micronaut-camunda-bpm/issues) or contact us via Gitter or mail.

We'd also like to hear from you if you're using the project :-)

Do you want to contact the core team?
* [Chat via Gitter](https://gitter.im/NovatecConsulting/micronaut-camunda-bpm) 
* [mailto:micronaut-camunda@novatec-gmbh.de](mailto:micronaut-camunda@novatec-gmbh.de)
