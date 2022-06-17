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

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        File[] files = outputSpecFile.listFiles();

        outputSpecFile = files[0];

        String answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertEquals("package de.microtema.model;\n" +
                "\n" +
                "import com.fasterxml.jackson.annotation.JsonProperty;\n" +
                "import lombok.EqualsAndHashCode;\n" +
                "import lombok.Data;\n" +
                "\n" +
                "/**\n" +
                "* Geschäftskunde\n" +
                "* Version: 1.0\n" +
                "*/\n" +
                "@Data\n" +
                "@EqualsAndHashCode(callSuper = true)\n" +
                "public class BusinessCustomer extends Customer {\n" +
                "\n" +
                "    /**\n" +
                "    * Der Firmenname\n" +
                "    */\n" +
                "\t@JsonProperty(\"COMPANY\")\n" +
                "    private String company;\n" +
                "\n" +
                "}\n", answer);

        outputSpecFile = files[1];

        answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertEquals("package de.microtema.model;\n" +
                "\n" +
                "import com.fasterxml.jackson.annotation.JsonProperty;\n" +
                "import de.microtema.commons.model.IdAble;\n" +
                "import de.microtema.commons.model.CarrierIdentifier;\n" +
                "import lombok.Data;\n" +
                "\n" +
                "@Data\n" +
                "public class Customer implements IdAble, CarrierIdentifier {\n" +
                "\n" +
                "    /**\n" +
                "    * Die eindeutige KundenID. Besteht aus einer 2-Zeichen Kennung für den Mandanten, gefolgt von einer Nummer.\n" +
                "    */\n" +
                "\t@JsonProperty(\"ID\")\n" +
                "    private String id;\n" +
                "\n" +
                "    /**\n" +
                "    * Der Mandant\n" +
                "    */\n" +
                "\t@JsonProperty(\"TENANT_ID\")\n" +
                "    private String tenantId;\n" +
                "\n" +
                "}\n", answer);

        outputSpecFile = files[2];

        answer = FileUtils.readFileToString(outputSpecFile, "UTF-8");

        assertEquals("package de.microtema.model;\n" +
                "\n" +
                "import com.fasterxml.jackson.annotation.JsonProperty;\n" +
                "import lombok.EqualsAndHashCode;\n" +
                "import lombok.Data;\n" +
                "\n" +
                "/**\n" +
                "* Privatekunde\n" +
                "* Version: 1.2\n" +
                "*/\n" +
                "@Data\n" +
                "@EqualsAndHashCode(callSuper = true)\n" +
                "public class PrivateCustomer extends Customer {\n" +
                "\n" +
                "    /**\n" +
                "    * Der Firmenname\n" +
                "    */\n" +
                "\t@JsonProperty(\"FIRST_NAME\")\n" +
                "    private String firstName;\n" +
                "\n" +
                "    /**\n" +
                "    * Der primäre Ansprechpartner\n" +
                "    */\n" +
                "\t@JsonProperty(\"CONTACTPERSON\")\n" +
                "    private String contactperson;\n" +
                "\n" +
                "}\n", answer);
    }
}
