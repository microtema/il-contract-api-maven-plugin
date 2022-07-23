package de.microtema.maven.plugin.contract.java.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ClassDescriptor {

    private String packageName;

    private EntityDescriptor entityDescriptor;

    private Map<String, String> fieldMapping;

    private String extendsClassName;

    private List<String> interfaceNames;

    private List<String> imports;

    private Set<String> commonFields;

    private boolean commonClass;
}
