package de.microtema.maven.plugin.contract.custom.model;

import lombok.Data;

import java.util.List;

@Data
public class EntityDescriptors {

    private List<EntityDescriptor> entities;
}
