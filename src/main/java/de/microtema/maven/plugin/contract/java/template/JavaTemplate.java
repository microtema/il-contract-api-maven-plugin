package de.microtema.maven.plugin.contract.java.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldType;
import de.microtema.maven.plugin.contract.util.MojoUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;


public class JavaTemplate {

    public static final String JSON_ANNOTATION = "@JsonProperty";

    public static boolean skipField(boolean isCommonClass, Set<String> commonFields, String name) {

        if (isCommonClass) {
            return !commonFields.contains(name);
        } else return commonFields.contains(name);
    }

    public static void appendDescription(StringBuilder stringBuilder, int padding, String... descriptions) {

        if (descriptions.length == 0) {
            return;
        }

        String paddingStr = "";

        while (paddingStr.length() < padding) {
            paddingStr += " ";
        }

        stringBuilder.append(paddingStr).append("/**").append(MojoUtil.lineSeparator(1));
        for (String description : descriptions) {
            stringBuilder.append(paddingStr).append(" * ").append(description).append(MojoUtil.lineSeparator(1));
        }
        stringBuilder.append(paddingStr).append(" */").append(MojoUtil.lineSeparator(1));
    }

    public static void appendClassDescription(StringBuilder stringBuilder, String... descriptions) {

        if (descriptions.length == 0) {
            return;
        }

        stringBuilder.append("/*").append(MojoUtil.lineSeparator(1));
        for (String description : descriptions) {
            stringBuilder.append(" * ").append(description).append(MojoUtil.lineSeparator(1));
        }
        stringBuilder.append(" */").append(MojoUtil.lineSeparator(1));
    }

    public static String getType(FieldType type) {

        switch (type) {
            case DATE:
                return "LocalDateTime";
            case TEXT:
                return "String";
            case NUMBER:
                return "Integer";
            case BOOLEAN:
                return "boolean";
            default:
                return "Object";
        }
    }

    public static String getFieldName(String snakeWord) {

        String camelWord = snakeWord.replaceAll("_", " ");

        camelWord = WordUtils.capitalizeFully(camelWord);

        camelWord = camelWord.replaceAll(" ", "");

        return WordUtils.uncapitalize(camelWord);
    }

    private void extendsImportPackages(List<FieldDescriptor> fieldDescriptors, Set<String> commonFields, boolean isCommonClass, List<String> imports, List<String> classImports) {

        boolean anyMatch = fieldDescriptors.stream()
                .filter(it -> !skipField(isCommonClass, commonFields, it.getName()))
                .anyMatch(it -> it.getType() == FieldType.DATE && StringUtils.equalsIgnoreCase(it.getTypeVariant(), "unixts_seconds"));

        if (anyMatch) {
            classImports.stream().filter(it -> StringUtils.contains(it, "EpochTimeSecondsToLocalDateTimeDeserializer")).forEach(imports::add);
        }

        anyMatch = fieldDescriptors.stream()
                .filter(it -> !skipField(isCommonClass, commonFields, it.getName()))
                .anyMatch(it -> it.getType() == FieldType.DATE);

        if (anyMatch) {
            imports.add("java.time.LocalDateTime");
        }
    }

    public static String getSimpleClassName(String className) {

        return className.substring(className.lastIndexOf(".") + 1);
    }

    private StringBuilder appendJsonKey(StringBuilder stringBuilder, String entryKey) {

        return stringBuilder.append(String.format("\t%s(\"%s\")", JSON_ANNOTATION, entryKey));
    }

    private StringBuilder appendJsonDeserialize(StringBuilder stringBuilder, String fieldType, String typeVariant) {

        if (StringUtils.isEmpty(typeVariant)) {
            return stringBuilder;
        }

        if (StringUtils.equals(fieldType, "LocalDateTime") && StringUtils.equals(typeVariant, "unixts_seconds")) {
            stringBuilder.append("    ").append("@JsonDeserialize(using = EpochTimeSecondsToLocalDateTimeDeserializer.class)").append(MojoUtil.lineSeparator(1));
        }

        return stringBuilder;
    }

    public void writeOutJavaFile(String outputDirectory, ClassDescriptor classDescriptor) throws IOException {

        String packageName = classDescriptor.getPackageName();
        EntityDescriptor entityDescriptor = classDescriptor.getEntityDescriptor();
        Map<String, String> fieldMapping = classDescriptor.getFieldMapping();
        String extendsClassName = classDescriptor.getExtendsClassName();
        List<String> interfaceNames = classDescriptor.getInterfaceNames();
        boolean isCommonClass = classDescriptor.isCommonClass();
        String className = isCommonClass ? extendsClassName : entityDescriptor.getName();
        Set<String> commonFields = classDescriptor.getCommonFields();

        StringBuilder stringBuilder = new StringBuilder();

        appendClassDescription(stringBuilder, "Generated by de.microtema:il-contract-api-maven-plugin");
        stringBuilder.append("package ").append(packageName).append(";").append(MojoUtil.lineSeparator(2));

        writeOutImports(entityDescriptor, classDescriptor, stringBuilder);

        if (!isCommonClass) {
            appendDescription(stringBuilder, 0, entityDescriptor.getDescription(), "Version: " + entityDescriptor.getVersion());
        }
        stringBuilder.append("@Data\n");
        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            stringBuilder.append("@EqualsAndHashCode(callSuper = true)").append(MojoUtil.lineSeparator(1));
        }
        stringBuilder.append("public class ").append(className).append(getImplementsOrExtendsClasses(extendsClassName, interfaceNames, isCommonClass)).append(" {").append(MojoUtil.lineSeparator(2));

        for (FieldDescriptor fieldDescriptor : entityDescriptor.getFields()) {

            String name = fieldDescriptor.getName();

            if (skipField(isCommonClass, commonFields, name)) {
                continue;
            }

            String cleanUpFieldName = fieldMapping.getOrDefault(name, name);

            String fieldName = getFieldName(cleanUpFieldName);
            String fieldType = getType(fieldDescriptor.getType());

            appendDescription(stringBuilder, 4, fieldDescriptor.getDescription());
            appendJsonKey(stringBuilder, name).append(MojoUtil.lineSeparator(1));
            appendJsonDeserialize(stringBuilder, fieldType, fieldDescriptor.getTypeVariant());
            stringBuilder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";").append(MojoUtil.lineSeparator(2));
        }

        stringBuilder.append("}").append(MojoUtil.lineSeparator(1));

        String packageDirectory = MojoUtil.getPackageDirectory(packageName);

        String file = String.format("%s%s%s%s.java", outputDirectory, File.separator, packageDirectory, className);
        System.out.println("Writing Java file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }

    private void writeOutImports(EntityDescriptor entityDescriptor, ClassDescriptor classDescriptor, StringBuilder stringBuilder) {

        String extendsClassName = classDescriptor.getExtendsClassName();
        List<String> interfaceNames = classDescriptor.getInterfaceNames();
        boolean isCommonClass = classDescriptor.isCommonClass();
        Set<String> commonFields = classDescriptor.getCommonFields();
        List<String> classImports = classDescriptor.getImports();

        List<String> imports = new ArrayList<>();

        boolean supportsJsonDeserialize = supportsJsonDeserialize(entityDescriptor.getFields(), commonFields, isCommonClass);

        imports.add("com.fasterxml.jackson.annotation.JsonProperty");
        if (supportsJsonDeserialize) {
            imports.add("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
        }

        imports.add("lombok.Data");
        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            imports.add("lombok.EqualsAndHashCode");
        }

        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            // do not implement interfaces on subclass, due to the common class
        } else {
            Collections.sort(interfaceNames);
            imports.addAll(interfaceNames);
        }

        extendsImportPackages(entityDescriptor.getFields(), commonFields, isCommonClass, imports, classImports);

        for (String importName : imports) {

            if (StringUtils.startsWith(importName, "java")) {
                stringBuilder.append(MojoUtil.lineSeparator(1));
            }

            stringBuilder.append("import ").append(importName).append(";").append(MojoUtil.lineSeparator(1));
        }

        stringBuilder.append(MojoUtil.lineSeparator(1));
    }

    private boolean supportsJsonDeserialize(List<FieldDescriptor> fields, Set<String> commonFields, boolean isCommonClass) {

        return fields.stream()
                .filter(it -> !skipField(isCommonClass, commonFields, it.getName()))
                .anyMatch(it -> StringUtils.isNotEmpty(it.getTypeVariant()));
    }

    private String getImplementsOrExtendsClasses(String extendsClassName, List<String> interfaceNames, boolean isCommonClass) {

        String str = "";

        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            return " extends " + getSimpleClassName(extendsClassName);
        }

        if (interfaceNames.isEmpty()) {
            return str;
        }

        return " implements " + StringUtils.join(interfaceNames.stream().map(JavaTemplate::getSimpleClassName).collect(Collectors.joining(", ")));
    }
}
