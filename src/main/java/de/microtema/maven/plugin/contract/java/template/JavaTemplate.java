package de.microtema.maven.plugin.contract.java.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class JavaTemplate {

    public static final String JSON_ANNOTATION = "@JsonProperty";

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

        stringBuilder.append("package ").append(packageName).append(";").append(FileUtil.lineSeparator(2));
        stringBuilder.append("import com.fasterxml.jackson.annotation.JsonProperty;").append(FileUtil.lineSeparator(1));

        for (String importPackageName : getImportPackages(entityDescriptor.getFields(), commonFields, isCommonClass)) {
            stringBuilder.append("import ").append(importPackageName).append(";").append(FileUtil.lineSeparator(1));
        }

        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            // do not implement interfaces on subclass, due to the common class
        } else {

            for (String importClassName : interfaceNames) {
                stringBuilder.append("import ").append(importClassName).append(";").append(FileUtil.lineSeparator(1));
            }
        }

        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            stringBuilder.append("import lombok.EqualsAndHashCode;").append(FileUtil.lineSeparator(1));
        }

        stringBuilder.append("import lombok.Data;").append(FileUtil.lineSeparator(2));

        if (!isCommonClass) {
            appendDescription(stringBuilder, 0, entityDescriptor.getDescription(), "Version: " + entityDescriptor.getVersion());
        }
        stringBuilder.append("@Data\n");
        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            stringBuilder.append("@EqualsAndHashCode(callSuper = true)").append(FileUtil.lineSeparator(1));
        }
        stringBuilder.append("public class ").append(className).append(getImplementsOrExtendsClasses(extendsClassName, interfaceNames, isCommonClass)).append(" {").append(FileUtil.lineSeparator(2));

        for (FieldDescriptor fieldDescriptor : entityDescriptor.getFields()) {

            String name = fieldDescriptor.getName();

            if (skipField(isCommonClass, commonFields, name)) {
                continue;
            }

            String cleanUpFieldName = fieldMapping.getOrDefault(name, name);

            String fieldName = getFieldName(cleanUpFieldName);
            String fieldType = getType(fieldDescriptor.getType());

            appendDescription(stringBuilder, 4, fieldDescriptor.getDescription());
            appendJsonKey(stringBuilder, name).append(FileUtil.lineSeparator(1)).append("    private ").append(fieldType).append(" ").append(fieldName).append(";").append(FileUtil.lineSeparator(2));
        }

        stringBuilder.append("}").append(FileUtil.lineSeparator(1));

        String packageDirectory = FileUtil.getPackageDirectory(packageName);

        String file = String.format("%s%s%s%s.java", outputDirectory, File.separator, packageDirectory, className);
        System.out.print("Writing file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }

    private boolean skipField(boolean isCommonClass, Set<String> commonFields, String name) {

        if (isCommonClass) {
            return !commonFields.contains(name);
        } else return commonFields.contains(name);
    }

    private String getImplementsOrExtendsClasses(String extendsClassName, List<String> interfaceNames, boolean isCommonClass) {

        String str = "";

        if (StringUtils.isNotEmpty(extendsClassName) && !isCommonClass) {
            return " extends " + getSimpleClassName(extendsClassName);
        }

        if (interfaceNames.isEmpty()) {
            return str;
        }

        return " implements " + StringUtils.join(interfaceNames.stream().map(this::getSimpleClassName).collect(Collectors.joining(", ")));
    }

    private void appendDescription(StringBuilder stringBuilder, int padding, String... descriptions) {

        if (descriptions.length == 0) {
            return;
        }

        String paddingStr = "";

        while (paddingStr.length() < padding) {
            paddingStr += " ";
        }

        stringBuilder.append(paddingStr).append("/**").append(FileUtil.lineSeparator(1));
        for (String description : descriptions) {
            stringBuilder.append(paddingStr).append("* ").append(description).append(FileUtil.lineSeparator(1));
        }
        stringBuilder.append(paddingStr).append("*/").append(FileUtil.lineSeparator(1));
    }

    private List<String> getImportPackages(List<FieldDescriptor> fieldDescriptors, Set<String> commonFields, boolean isCommonClass) {

        List<String> packages = new ArrayList<>();

        boolean anyMatch = fieldDescriptors.stream()
                .filter(it -> !skipField(isCommonClass, commonFields, it.getName()))
                .anyMatch(it -> it.getType() == FieldType.DATE);

        if (anyMatch) {
            packages.add("java.time.LocalDateTime");
        }

        return packages;
    }

    private String getType(FieldType type) {

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

    private StringBuilder appendJsonKey(StringBuilder stringBuilder, String entryKey) {

        return stringBuilder.append(String.format("\t%s(\"%s\")", JSON_ANNOTATION, entryKey));
    }

    private String getFieldName(String snakeWord) {

        String camelWord = snakeWord.replaceAll("_", " ");

        camelWord = WordUtils.capitalizeFully(camelWord);

        camelWord = camelWord.replaceAll(" ", "");

        return WordUtils.uncapitalize(camelWord);
    }

    private String getSimpleClassName(String className) {

        return className.substring(className.lastIndexOf(".") + 1);
    }
}
