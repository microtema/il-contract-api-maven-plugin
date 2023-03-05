package de.microtema.maven.plugin.contract.java.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import de.microtema.maven.plugin.contract.doc.template.AsciiDocTemplate;
import de.microtema.maven.plugin.contract.doc.template.DocTemplate;
import de.microtema.maven.plugin.contract.doc.template.MDDocTemplate;
import de.microtema.maven.plugin.contract.model.ProjectData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@RequiredArgsConstructor
public class JavaTemplateService {

    private final JavaTemplate javaTemplate;
    private final MDDocTemplate MDDocTemplate;
    private final AsciiDocTemplate asciiDocTemplate;

    private DocTemplate getDocTemplate(String templateType) {

        if (asciiDocTemplate.access(templateType)) {
            return asciiDocTemplate;
        }

        return MDDocTemplate;
    }

    @SneakyThrows
    public void writeJavaTemplates(List<List<EntityDescriptor>> allEntities, ProjectData projectData) {

        Set<String> fileNames = new HashSet<>();

        for (List<EntityDescriptor> entities : allEntities) {

            writeJavaTemplate(entities, allEntities, projectData, fileNames);
        }
    }

    @SneakyThrows
    public void writeJavaTemplate(List<EntityDescriptor> entities, List<List<EntityDescriptor>> allEntities, ProjectData projectData, Set<String> set) {

        String domainName = projectData.getDomainName();

        for (EntityDescriptor entityDescriptor : entities) {

            if (allEntities.size() == 1) {
                entities.forEach(it -> it.setName(Optional.ofNullable(domainName).orElse(it.getName())));
            }

            String extendsClassName = null;
            Set<String> commonFields = new HashSet<>();

            ExtendsClass extendsClass = getExtendsClass(allEntities, entityDescriptor.getName());

            if (Objects.nonNull(extendsClass)) {

                extendsClassName = Optional.ofNullable(domainName).orElse(extendsClass.getName());
                commonFields = extendsClass.getFields();

                if (set.add(extendsClassName)) {
                    writeJavaTemplateImpl(entities, extendsClassName, commonFields, true, projectData);
                }
            }

            writeJavaTemplateImpl(entities, extendsClassName, commonFields, false, projectData);
        }
    }

    @SneakyThrows
    public void writeJavaTemplateImpl(List<EntityDescriptor> entities, String extendsClassName, Set<String> commonFields, boolean isCommonClass, ProjectData projectData) {

        String domainName = projectData.getDomainName();

        DocTemplate docTemplate = getDocTemplate(projectData.getTemplateType());

        for (EntityDescriptor entityDescriptor : entities) {

            ClassDescriptor classDescriptor = new ClassDescriptor();

            classDescriptor.setPackageName(projectData.getPackageName());
            classDescriptor.setEntityDescriptor(entityDescriptor);
            classDescriptor.setFieldMapping(projectData.getFieldMapping());

            classDescriptor.setExtendsClassName(extendsClassName);

            if (StringUtils.isNotEmpty(extendsClassName)) {
                classDescriptor.setExtendsClassName(domainName);
            }

            classDescriptor.setImports(projectData.getImports());
            classDescriptor.setInterfaceNames(projectData.getInterfaceNames());
            classDescriptor.setCommonFields(commonFields);
            classDescriptor.setCommonClass(isCommonClass);

            javaTemplate.writeOutJavaFile(projectData.getOutputJavaDirectory(), classDescriptor);

            docTemplate.writeOutDocFile(projectData.getOutputDocDirectory(), classDescriptor);
        }
    }

    private ExtendsClass getExtendsClass(List<List<EntityDescriptor>> allEntities, String entityName) {

        Map<String, Set<String>> map = new HashMap<>();

        for (List<EntityDescriptor> entities : allEntities) {

            Map<String, Set<String>> commonFields = getCommonFields(entities);

            for (Map.Entry<String, Set<String>> entries : commonFields.entrySet()) {

                String fieldName = entries.getKey();

                Set<String> classes = map.get(fieldName);

                if (Objects.isNull(classes)) {
                    classes = entries.getValue();
                    map.put(fieldName, classes);
                }

                classes.addAll(entries.getValue());
            }
        }

        Map<String, Set<String>> commonClasses = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {

            String fieldName = entry.getKey();
            Set<String> classes = entry.getValue();

            if (classes.size() == 1 || !classes.contains(entityName)) {
                continue;
            }

            String commonClassName = getCommonClassName(classes);

            Set<String> commonFields = commonClasses.get(commonClassName);

            if (Objects.isNull(commonFields)) {
                commonFields = new HashSet<>();

                commonClasses.put(commonClassName, commonFields);
            }

            commonFields.add(fieldName);
        }

        for (Map.Entry<String, Set<String>> entries : commonClasses.entrySet()) {

            ExtendsClass extendsClass = new ExtendsClass();

            extendsClass.setName(entries.getKey());
            extendsClass.setFields(entries.getValue());

            return extendsClass;
        }

        return null;
    }

    private String getCommonClassName(Set<String> classes) {

        for (String className : classes) {
            String[] parts = className.split("(?=\\p{Upper})");
            return parts[parts.length - 1];
        }

        return "Base";
    }

    private Map<String, Set<String>> getCommonFields(List<EntityDescriptor> entities) {

        Map<String, Set<String>> map = new HashMap<>();

        for (EntityDescriptor entityDescriptor : entities) {

            String name = entityDescriptor.getName();

            List<FieldDescriptor> fields = entityDescriptor.getFields();

            for (FieldDescriptor fieldDescriptor : fields) {

                String fieldName = fieldDescriptor.getName();
                Set<String> classes = map.get(fieldName);

                if (Objects.isNull(classes)) {
                    classes = new HashSet<>();
                    map.put(fieldName, classes);
                }

                classes.add(name);
            }
        }

        return map;
    }
}
