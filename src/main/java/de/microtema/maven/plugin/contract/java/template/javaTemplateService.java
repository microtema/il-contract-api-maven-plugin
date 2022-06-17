package de.microtema.maven.plugin.contract.java.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.*;

@RequiredArgsConstructor
public class javaTemplateService {

    private final JavaTemplate javaTemplate;

    @SneakyThrows
    public void writeJavaTemplates(String outputDirectory, String packageName, List<List<EntityDescriptor>> allEntities, Map<String, String> fieldMapping, List<String> interfaceNames) {

        for (List<EntityDescriptor> entities : allEntities) {

            writeJavaTemplate(outputDirectory, packageName, entities, allEntities, fieldMapping, interfaceNames);
        }
    }

    @SneakyThrows
    public void writeJavaTemplate(String outputDirectory, String packageName, List<EntityDescriptor> entities, List<List<EntityDescriptor>> allEntities, Map<String, String> fieldMapping, List<String> interfaceNames) {

        for (EntityDescriptor entityDescriptor : entities) {

            String extendsClassName = null;
            Set<String> commonFields = new HashSet<>();

            ExtendsClass extendsClass = getExtendsClass(allEntities, entityDescriptor.getName());

            if (Objects.nonNull(extendsClass)) {

                extendsClassName = extendsClass.getName();
                commonFields = extendsClass.getFields();

                writeJavaTemplateImpl(outputDirectory, packageName, entities, fieldMapping, extendsClassName, interfaceNames, commonFields, true);
            }

            writeJavaTemplateImpl(outputDirectory, packageName, entities, fieldMapping, extendsClassName, interfaceNames, commonFields, false);
        }
    }

    @SneakyThrows
    public void writeJavaTemplateImpl(String outputDirectory, String packageName, List<EntityDescriptor> entities, Map<String, String> fieldMapping, String extendsClassName, List<String> interfaceNames, Set<String> commonFields, boolean isCommonClass) {

        for (EntityDescriptor entityDescriptor : entities) {

            ClassDescriptor classDescriptor = new ClassDescriptor();

            classDescriptor.setPackageName(packageName);
            classDescriptor.setEntityDescriptor(entityDescriptor);
            classDescriptor.setFieldMapping(fieldMapping);

            classDescriptor.setExtendsClassName(extendsClassName);
            classDescriptor.setInterfaceNames(interfaceNames);
            classDescriptor.setCommonFields(commonFields);
            classDescriptor.setCommonClass(isCommonClass);

            javaTemplate.writeOutJavaFile(outputDirectory, classDescriptor);
        }
    }

    private ExtendsClass getExtendsClass(List<List<EntityDescriptor>> allEntities, String entityName) {

        Map<String, Set<String>> map = new HashMap<>();

        for (List<EntityDescriptor> entities : allEntities) {

            Map<String, Set<String>> commonFields = getCommonFields(entities, entityName);

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

    private Map<String, Set<String>> getCommonFields(List<EntityDescriptor> entities, String entityName) {

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
