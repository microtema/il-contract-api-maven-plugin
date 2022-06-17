package de.microtema.maven.plugin.contract.custom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityDescriptor {

    private String name;

    private String description;

    private String version;

    @JsonProperty("system_id")
    private String system;

    private List<FieldDescriptor> fields;
}
