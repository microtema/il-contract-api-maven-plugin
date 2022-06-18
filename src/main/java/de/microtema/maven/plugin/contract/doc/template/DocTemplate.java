package de.microtema.maven.plugin.contract.doc.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import de.microtema.maven.plugin.contract.java.template.ClassDescriptor;
import de.microtema.maven.plugin.contract.java.template.FileUtil;
import de.microtema.maven.plugin.contract.java.template.JavaTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

public class DocTemplate {

    public void writeOutDocFile(String outputDirectory, ClassDescriptor classDescriptor) throws IOException {

        EntityDescriptor entityDescriptor = classDescriptor.getEntityDescriptor();
        String extendsClassName = classDescriptor.getExtendsClassName();

        boolean isCommonClass = classDescriptor.isCommonClass();
        String className = isCommonClass ? extendsClassName : entityDescriptor.getName();
        Set<String> commonFields = classDescriptor.getCommonFields();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("# ").append(className).append(FileUtil.lineSeparator(2));

        if (isCommonClass) {
            stringBuilder.append("> ").append(className).append(" Base class").append(FileUtil.lineSeparator(2));
        } else {
            stringBuilder.append("> ").append(entityDescriptor.getDescription()).append(FileUtil.lineSeparator(2));
            stringBuilder.append("> ").append("Version: ").append(entityDescriptor.getVersion()).append(FileUtil.lineSeparator(2));
        }

        stringBuilder.append("| Name | Type | Required | Default Value | Description |").append(FileUtil.lineSeparator(1));
        stringBuilder.append("| --- | --- | --- | --- | --- |").append(FileUtil.lineSeparator(1));

        for (FieldDescriptor fieldDescriptor : entityDescriptor.getFields()) {

            String name = fieldDescriptor.getName();

            if (JavaTemplate.skipField(isCommonClass, commonFields, name)) {
                continue;
            }

            String fieldType = JavaTemplate.getType(fieldDescriptor.getType());
            String description = fieldDescriptor.getDescription();
            boolean required = fieldDescriptor.isRequired();
            String defaultValue = StringUtils.trimToEmpty(fieldDescriptor.getDefaultValue());

            stringBuilder.append("| ").append(name).append(" | ").append(fieldType).append(" | ").append(required).append(" | ").append(defaultValue).append(" | ").append(description).append(" |").append(FileUtil.lineSeparator(1));
        }

        String file = String.format("%s%s%s.md", outputDirectory, File.separator, className);

        System.out.println("Writing Doc file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }
}
