package de.microtema.maven.plugin.contract.java.template;

import lombok.Data;

import java.util.Set;

@Data
public class ExtendsClass {

    private String name;

    private Set<String> fields;
}
