package de.microtema.maven.plugin.contract.custom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecificationDescriptor {

    private String eventCategory;
    private String eventName;
    private String eventVersion;
    private String eventDescription;

    @JsonProperty("payloadSchema")
    private List<FieldDescriptor> fields;
}
