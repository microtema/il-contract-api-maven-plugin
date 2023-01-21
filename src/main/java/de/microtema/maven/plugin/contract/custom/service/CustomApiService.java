package de.microtema.maven.plugin.contract.custom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import de.microtema.maven.plugin.contract.custom.converter.SpecificationDescriptorToEntityDescriptionConverter;
import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.EntityDescriptors;
import de.microtema.maven.plugin.contract.custom.model.SpecificationDescriptors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public class CustomApiService {

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    private final SpecificationDescriptorToEntityDescriptionConverter entityDescriptionConverter;

    @SneakyThrows
    public List<EntityDescriptor> getEntityDescriptors(String fileName) {

        File file = new File(fileName);

        EntityDescriptors entityDescriptors = objectMapper.readValue(file, EntityDescriptors.class);

        List<EntityDescriptor> entities = entityDescriptors.getEntities();

        if (!CollectionUtils.isEmpty(entities)) {
            return entities;
        }

        SpecificationDescriptors specificationDescriptors = objectMapper.readValue(file, SpecificationDescriptors.class);

        return entityDescriptionConverter.convertList(specificationDescriptors.getSpecifications());
    }
}
