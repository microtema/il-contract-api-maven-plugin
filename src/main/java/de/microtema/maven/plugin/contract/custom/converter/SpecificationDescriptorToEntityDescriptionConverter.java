package de.microtema.maven.plugin.contract.custom.converter;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.SpecificationDescriptor;
import de.microtema.model.converter.Converter;

public class SpecificationDescriptorToEntityDescriptionConverter implements Converter<EntityDescriptor, SpecificationDescriptor> {

    @Override
    public void update(EntityDescriptor dest, SpecificationDescriptor orig) {

        dest.setName(orig.getEventCategory());
        dest.setDescription(orig.getEventDescription());
        dest.setVersion(orig.getEventVersion());
        dest.setFields(orig.getFields());
    }
}
