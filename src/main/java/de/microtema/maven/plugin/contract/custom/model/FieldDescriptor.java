package de.microtema.maven.plugin.contract.custom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDescriptor {

    private String name;

    private String reference;

    private FieldType type;

    private String description;

    public boolean required;

    private int length;
}
