package de.microtema.maven.plugin.contract;

import de.microtema.maven.plugin.contract.java.template.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ApiContractGeneratorMojoTest {

    @InjectMocks
    ApiContractGeneratorMojo sut;

    @Mock
    MavenProject project;

    File outputSpecFile;

    @BeforeEach
    void setUp() {

        sut.project = project;
    }

    @Test
    void executeOnNonUpdateFalse() throws Exception {

        String packageDirectory = FileUtil.getPackageDirectory(sut.packageName);

        outputSpecFile = new File(sut.outputDir, packageDirectory);

        sut.implementations.add("de.microtema.commons.model.IdAble");
        sut.implementations.add("de.microtema.commons.model.CarrierIdentifier");

        sut.execute();

        outputSpecFile = outputSpecFile.listFiles()[0];

        String answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertNotNull(answer);
    }
}
