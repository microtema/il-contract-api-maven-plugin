# Integration Layer Contract API Generator
Reducing Boilerplate Code with il-contract-api maven plugin
> More Time for Feature and functionality
  Through a simple set of il-contract-api templates and saving 60% of development time 

## Key Features
* Auto generation by maven compile phase
* Auto detection commons classes
* Generate lombok based java beans
* Custom Field mapping

## How to use

```
<plugin>
    <groupId>de.microtema</groupId>
    <artifactId>il-contract-api-maven-plugin</artifactId>
    <version>2.0.1</version>
    <configuration>
        <packageName>${project.groupId}.model</packageName>
        <outputDir>./src/main/java</outputDir>
        <fieldMapping>
            <LASTNAME>LAST_NAME</LASTNAME>
            <FIRSTNAME>FIRST_NAME</FIRSTNAME>
            <MODIFIED_AT>MODIFIED_DATE</MODIFIED_AT>
            <CREATED_AT>CREATED_DATE</CREATED_AT>
        </fieldMapping>
    </configuration>
    <executions>
        <execution>
            <id>il-contract-api</id>
            <phase>validate</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Output 
* ./target/generated/src/main/de/microtema/model/BusinessCustomer.java 
* ./target/generated/src/main/de/microtema/model/Customer.java 
* ./target/generated/src/main/de/microtema/model/PrivateCustomer.java 

> NOTE: This is an example file.

```
package de.microtema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
* Business Customer
* Version: 1.0
*/
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessCustomer extends Customer {

    /**
    * Comany Name
    */
	@JsonProperty("COMPANY")
    private String company;

}
```

```
package de.microtema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.microtema.commons.model.IdAble;
import de.microtema.commons.model.CarrierIdentifier;
import lombok.Data;

@Data
public class Customer implements IdAble, CarrierIdentifier {

    /**
    * Customer ID
    */
	@JsonProperty("ID")
    private String id;

    /**
    * Tenant ID
    */
	@JsonProperty("TENANT_ID")
    private String tenantId;

    /**
    * Carrier Identifier
    */
	@JsonProperty("CARRIER_IDENTIFIER")
    private String carrierIdentifier;

}
```

```
package de.microtema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
* Private Customer
* Version: 1.2
*/
@Data
@EqualsAndHashCode(callSuper = true)
public class PrivateCustomer extends Customer {

    /**
    * Company Name
    */
	@JsonProperty("FIRST_NAME")
    private String firstName;

    /**
    * Primary contact person
    */
	@JsonProperty("CONTACTPERSON")
    private String contactperson;

}
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
