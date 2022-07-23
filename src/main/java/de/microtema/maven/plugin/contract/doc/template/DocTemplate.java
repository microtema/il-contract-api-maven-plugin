package de.microtema.maven.plugin.contract.doc.template;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.model.FieldDescriptor;
import de.microtema.maven.plugin.contract.java.template.ClassDescriptor;
import de.microtema.maven.plugin.contract.java.template.JavaTemplate;
import de.microtema.maven.plugin.contract.util.MojoUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class DocTemplate {

    public void writeOutDocFile(String outputDirectory, ClassDescriptor classDescriptor) throws IOException {

        EntityDescriptor entityDescriptor = classDescriptor.getEntityDescriptor();
        String extendsClassName = classDescriptor.getExtendsClassName();

        boolean isCommonClass = classDescriptor.isCommonClass();
        String className = isCommonClass ? extendsClassName : entityDescriptor.getName();
        Set<String> commonFields = classDescriptor.getCommonFields();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("# ").append(className).append(MojoUtil.lineSeparator(2));

        if (isCommonClass) {
            stringBuilder.append("> ").append(className).append(" Base class").append(MojoUtil.lineSeparator(2));

            List<String> interfaceNames = classDescriptor.getInterfaceNames();

            if (!interfaceNames.isEmpty()) {
                stringBuilder.append("## ").append("Implements: ").append(MojoUtil.lineSeparator(1));
            }

            for (String interfaceName : interfaceNames) {
                stringBuilder.append("* ").append(MojoUtil.cleanUp(interfaceName)).append(MojoUtil.lineSeparator(1));
            }

            if (!interfaceNames.isEmpty()) {
                stringBuilder.append(MojoUtil.lineSeparator(1));
            }

        } else {
            stringBuilder.append("> ").append(entityDescriptor.getDescription()).append(MojoUtil.lineSeparator(2));
            stringBuilder.append("> ").append("Version: ").append(entityDescriptor.getVersion()).append(MojoUtil.lineSeparator(2));
            if (StringUtils.isNotEmpty(extendsClassName)) {
                stringBuilder.append("> ").append("Extends: ").append("[@").append(extendsClassName).append("](").append(extendsClassName).append(".md)").append(MojoUtil.lineSeparator(2));
            }
        }

        stringBuilder.append("| # | Name | Type | Required | Length | Description |").append(MojoUtil.lineSeparator(1));
        stringBuilder.append("| --- | --- | --- | --- | --- | --- |").append(MojoUtil.lineSeparator(1));

        int index = 0;

        for (FieldDescriptor fieldDescriptor : entityDescriptor.getFields()) {

            String name = fieldDescriptor.getName();

            if (JavaTemplate.skipField(isCommonClass, commonFields, name)) {
                continue;
            }

            String fieldType = JavaTemplate.getType(fieldDescriptor.getType());
            String description = fieldDescriptor.getDescription();
            boolean required = fieldDescriptor.isRequired();
            int lengthValue = fieldDescriptor.getLength();

            String requiredValue = required ? "Yes" : "";

            stringBuilder.append("| ").append(index++)
                    .append("| ").append(name)
                    .append(" | ").append(fieldType)
                    .append(" | ").append(requiredValue)
                    .append(" | ").append(lengthValue > 0 ? lengthValue : "")
                    .append(" | ").append(description)
                    .append(" |").append(MojoUtil.lineSeparator(1));
        }

        String file = String.format("%s%s%s.md", outputDirectory, File.separator, className);

        System.out.println("Writing Doc file " + file);

        File outputFile = new File(file);
        FileUtils.writeStringToFile(outputFile, stringBuilder.toString(), Charset.defaultCharset());
    }
}
