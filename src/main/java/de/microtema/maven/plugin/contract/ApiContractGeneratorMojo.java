package de.microtema.maven.plugin.contract;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.service.CustomApiService;
import de.microtema.maven.plugin.contract.java.template.javaTemplateService;
import de.microtema.maven.plugin.contract.model.ProjectData;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.*;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class ApiContractGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "api-file-name")
    String apiFileName = "./src/main/resources/api";

    @Parameter(property = "output-dir")
    String outputDir = "./target/generated/src/main";

    @Parameter(property = "output-doc-dir")
    String outputDocDir = "./apidocs";

    @Parameter(property = "package-name")
    String packageName = "de.microtema.model";

    @Parameter(property = "implementations")
    List<String> implementations = new ArrayList<>();

    @Parameter(property = "field-mapping")
    Map<String, String> fieldMapping = new HashMap<>();

    CustomApiService customApiService = ClassUtil.createInstance(CustomApiService.class);

    javaTemplateService javaTemplateService = ClassUtil.createInstance(javaTemplateService.class);

    @SneakyThrows
    public void execute() {

        String appName = Optional.ofNullable(project.getName()).orElse(project.getArtifactId());

        // Skip maven sub modules
        if (StringUtils.isEmpty(apiFileName)) {

            logMessage("Skip maven module: " + appName + " since it does not contains api file!");

            return;
        }

        File fileOrDir = new File(apiFileName);

        if (!fileOrDir.exists()) {

            logMessage("Skip maven module: " + appName + " since it does not contains api file!");

            return;
        }

        logMessage("Generate GitLab Pipeline for " + appName + " -> " + outputDir);

        List<List<EntityDescriptor>> all = new ArrayList<>();

        if (fileOrDir.isFile()) {

            List<EntityDescriptor> entities = customApiService.getEntityDescriptors(fileOrDir.getPath());

            all.add(entities);

        } else if (fileOrDir.isDirectory()) {

            File[] files = fileOrDir.listFiles();

            if (Objects.isNull(files) || files.length == 0) {

                logMessage("Skip maven module: " + appName + " since it does not contains api file!");

                return;
            }

            for (File file : files) {

                List<EntityDescriptor> entities = customApiService.getEntityDescriptors(file.getPath());

                all.add(entities);
            }
        }

        ProjectData projectData = new ProjectData();

        projectData.setPackageName(this.packageName);
        projectData.setInterfaceNames(this.implementations);
        projectData.setFieldMapping(this.fieldMapping);

        projectData.setOutputJavaDirectory(this.outputDir);
        projectData.setOutputDocDirectory(this.outputDocDir);

        javaTemplateService.writeJavaTemplates(all, projectData);
    }

    void logMessage(String message) {

        Log log = getLog();

        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }
}
