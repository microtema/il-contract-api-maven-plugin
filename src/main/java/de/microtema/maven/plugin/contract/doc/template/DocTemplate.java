package de.microtema.maven.plugin.contract.doc.template;

import de.microtema.maven.plugin.contract.java.template.ClassDescriptor;

import java.io.IOException;

public interface DocTemplate {

    boolean access(String templateType);

    void writeOutDocFile(String outputDirectory, ClassDescriptor classDescriptor) throws IOException;
}
