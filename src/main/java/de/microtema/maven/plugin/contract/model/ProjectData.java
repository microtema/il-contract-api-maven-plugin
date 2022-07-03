package de.microtema.maven.plugin.contract.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProjectData {

    private String outputJavaDirectory;

    private String outputDocDirectory;

    private String packageName;

    private Map<String, String> fieldMapping;

    private List<String> interfaceNames;

    private String domainName;
}
