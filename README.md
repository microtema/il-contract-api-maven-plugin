# gitlab ci generator
Reducing Boilerplate Code with gitlabci maven plugin
> More Time for Feature and functionality
  Through a simple set of gitlabci templates and saving 60% of development time 

## Key Features
* Auto generate by maven compile phase
* Auto JUnit Tests detector by adding "JUnit Tests" stage
* Auto Integration Tests detector by adding "Integration Tests" stage
* Auto Dockerfile detector by adding "Build Docker" stage
* Auto Maven artifact detector by adding "Deploy Maven Artifact" stage
* Auto Sonar report detector by adding "Sonar Report" stage
* Auto Deployment to Cloud Platform by adding "Deployment" stage


## How to use

```
<plugin>
    <groupId>de.microtema</groupId>
    <artifactId>gitlabci-maven-plugin</artifactId>
    <version>2.0.1-SNAPSHOT</version>
    <configuration>
        <variables>
          <DOCKER_REGISTRY>docker.registry.local</DOCKER_REGISTRY>
        </variables>
        <stages>
            <dev>develop</dev>
            <stage>/^release/.*$/</stage>
        </stages>
    </configuration>
    <executions>
        <execution>
            <id>gitlabci</id>
            <phase>compile</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Output 
> .gitlab-ci.yml 
> NOTE: This is an example file.

```
variables:
  DOCKER_REGISTRY: "docker.registry.local"
  GIT_STRATEGY: "clone"
  GIT_DEPTH: "10"
  MAVEN_OPTS: "-Dhttps.protocols=TLSv1.2 -Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository\
    \ -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN\
    \ -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
  MAVEN_CLI_OPTS: "-s settings.xml --batch-mode --errors --fail-at-end --show-version\
    \ -DinstallAtEnd=true -DdeployAtEnd=true -Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT\
    \ -Dhttp.nonProxyHosts=$NO_PROXY -Dhttps.proxyHost=$PROXY_HOST -Dhttps.proxyPort=$PROXY_PORT\
    \ -Dhttps.nonProxyHosts=$NO_PROXY"

services:
  - name: "docker:19.03.15-dind"
    command:
      - "--insecure-registry=registry.docker.versatel.local"

cache: &project-cache
  key: "$CI_PROJECT_ID"
  paths:
    - "$CI_PROJECT_DIR/.m2/repository"

.stage-template: &stage-template
  image: "maven:3-openjdk-15"

stages:
  - compile
  - security
  - versioning
  - test
  - quality
  - build
  - package
  - publish
  - deployment
  - readiness
  - regression
  - performance
  - report

Compile:
  <<: *stage-template
  stage: compile
  script:
    - mvn compile $MAVEN_CLI_OPTS

Security Check:
  <<: *stage-template
  stage: security
  script:
    - mvn dependency-check:help -P security -Ddownloader.quick.query.timestamp=false $MAVEN_CLI_OPTS

Versioning Release:
  <<: *stage-template
  stage: versioning
  script:
    - echo 'mvn versioning'
  only:
    refs:
      - /^release/.*$/

Versioning Master:
  <<: *stage-template
  stage: versioning
  script:
    - echo 'mvn versioning'
  only:
    refs:
      - master
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/

Unit Test:
  <<: *stage-template
  stage: test
  cache:
    <<: *project-cache
    policy: pull
  script:
    - mvn test $MAVEN_CLI_OPTS
  artifacts:
    paths:
      - target
    expire_in: 6 hour

Acceptance Test:
  <<: *stage-template
  stage: test
  cache:
    <<: *project-cache
    policy: pull
  script:
    - mvn test-compile failsafe:integration-test $MAVEN_CLI_OPTS
  artifacts:
    paths:
      - target
    expire_in: 6 hour

Quality Gate:
  <<: *stage-template
  stage: quality
  script:
    - mvn sonar:sonar $MAVEN_CLI_OPTS
  only:
    refs:
      - develop
      - /^feature/.*$/
      - /^release/.*$/
      - master
  artifacts:
    paths:
      - target
    expire_in: 6 hour

Build:
  <<: *stage-template
  stage: build
  cache:
    <<: *project-cache
    policy: pull-push
  artifacts:
    paths:
      - target
    expire_in: 6 hour
  script:
    - mvn package -P prod -Dcode.coverage=0.0 -DskipTests=true $MAVEN_CLI_OPTS

Package:
  image: docker:19.03.12
  stage: package
  dependencies:
    - Build
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG
  only:
    refs:
      - develop
      - /^release/.*$/
      - master
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/

Deployment:
  stage: deployment
  script:
    - echo 'trigger other *-deployments pipeline'
  only:
    refs:
      - develop
      - /^release/.*$/
      - master
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/

Readiness Check:
  stage: readiness
  script:
    - echo 'check sonar qualitygate'
  only:
    refs:
      - develop
      - /^release/.*$/
      - master
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/

I2E:DEV:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P i2e -DstageName=dev $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ develop ]

E2E:DEV:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P e2e -DstageName=dev $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ develop ]

S2E:DEV:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P s2e -DstageName=dev $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ develop ]

I2E:STAGE:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P i2e -DstageName=stage $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ release/*, bugfix/* ]

E2E:STAGE:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P e2e -DstageName=stage $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ release/*, bugfix/* ]

S2E:STAGE:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P s2e -DstageName=stage $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ release/*, bugfix/* ]

I2E:PRD:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P i2e -DstageName=prd $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ master ]

E2E:PRD:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P e2e -DstageName=prd $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ master ]

S2E:PRD:
   <<: *stage-template
   stage: regression
   script:
     - mvn integration-test -P s2e -DstageName=prd $MAVEN_CLI_OPTS
   artifacts:
     paths:
       - target
     expire_in: 6 hour
   only:
     refs: [ master ]

Load and Performance:
  <<: *stage-template
  stage: performance
  script:
    - echo 'mvn validate -P performace-$STAGE_NAME $MAVEN_CLI_OPTS'
  artifacts:
    paths:
      - target
    expire_in: 6 hour
  only:
    refs:
      - develop
      - /^release/.*$/

Test Report:
  stage: report
  script:
    - echo 'mvn report -Preport'
  only:
    refs:
      - develop
      - /^release/.*$/
      - master
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/

Change Log:
  <<: *stage-template
  stage: report
  script:
    - echo 'mvn changelog -P changelog $MAVEN_CLI_OPTS'
  only:
    refs:
      - master
      - /^release/.*$/
      - /^v[0-9]+\.[0-9]+\.[0-9]+$/
```
    
## Technology Stack

* Java 1.8
    * Streams 
    * Lambdas
* Third Party Libraries
    * Commons-BeanUtils (Apache License)
    * Commons-IO (Apache License)
    * Commons-Lang3 (Apache License)
    * Junit (EPL 1.0 License)
* Code-Analyses
    * Sonar
    * Jacoco
    
## Test Coverage threshold
> 95%
    
## License

MIT (unless noted otherwise)

## Quality Gate Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)

[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=mtema_jenkinsfile-maven-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=mtema_jenkinsfile-maven-plugin)
