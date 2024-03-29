package de.microtema.maven.plugin.contract.custom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityDescriptors {

    private List<EntityDescriptor> entities;
}
