package de.microtema.maven.plugin.contract.custom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.EntityDescriptors;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class CustomApiService {

   private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    @SneakyThrows
    public List<EntityDescriptor> getEntityDescriptors(String fileName) {

        EntityDescriptors entityDescriptors = objectMapper.readValue(new File(fileName), EntityDescriptors.class);

        return entityDescriptors.getEntities();
    }
}
