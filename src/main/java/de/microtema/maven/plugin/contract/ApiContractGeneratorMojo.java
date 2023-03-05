package de.microtema.maven.plugin.contract;

import de.microtema.maven.plugin.contract.custom.model.EntityDescriptor;
import de.microtema.maven.plugin.contract.custom.service.CustomApiService;
import de.microtema.maven.plugin.contract.java.template.JavaTemplateService;
import de.microtema.maven.plugin.contract.model.ProjectData;
import de.microtema.maven.plugin.contract.util.MojoUtil;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class ApiContractGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "api-file-name")
    String apiFileName = "./src/main/resources/api";

    @Parameter(property = "output-dir")
    String outputDir = "./target/generated/src/main";

    @Parameter(property = "output-doc-dir")
    String outputDocDir = "./apidocs/model";

    @Parameter(property = "package-name")
    String packageName = "de.microtema.model";

    @Parameter(property = "implementations")
    List<String> implementations = new ArrayList<>();

    @Parameter(property = "imports")
    List<String> imports = new ArrayList<>();

    @Parameter(property = "excludes")
    Set<String> excludes = new HashSet<>();

    @Parameter(property = "field-mapping")
    Set<String> fieldMapping = new HashSet<>();

    @Parameter(property = "domain-name")
    String domainName;

    CustomApiService customApiService = ClassUtil.createInstance(CustomApiService.class);

    JavaTemplateService javaTemplateService = ClassUtil.createInstance(JavaTemplateService.class);

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

        logMessage("Generate API Contract for " + appName + " -> " + outputDir);

        List<List<EntityDescriptor>> all = new ArrayList<>();

        if (fileOrDir.isFile()) {

            List<EntityDescriptor> entities = customApiService.getEntityDescriptors(fileOrDir.getPath());

            all.add(entities);

        } else if (fileOrDir.isDirectory()) {

            File[] files = fileOrDir.listFiles(it -> !it.getName().startsWith(".")); // filter out system files like .DS_Store

            if (Objects.isNull(files) || files.length == 0) {

                logMessage("Skip maven module: " + appName + " since it does not contains api file!");

                return;
            }

            for (File file : files) {

                List<EntityDescriptor> entities = customApiService.getEntityDescriptors(file.getPath());

                all.add(entities);
            }
        }

        excludesProperties(all);

        ProjectData projectData = new ProjectData();

        projectData.setPackageName(packageName);
        projectData.setInterfaceNames(implementations.stream().map(StringUtils::trim).collect(Collectors.toList()));
        projectData.setImports(imports.stream().map(StringUtils::trim).collect(Collectors.toList()));
        projectData.setFieldMapping(streamConvert(fieldMapping));

        projectData.setOutputJavaDirectory(outputDir);
        projectData.setOutputDocDirectory(outputDocDir);
        projectData.setDomainName(Optional.ofNullable(domainName).map(WordUtils::capitalize).orElse(null));
        projectData.setTemplateType(MojoUtil.getTemplateType(project.getBasedir()));

        javaTemplateService.writeJavaTemplates(all, projectData);
    }

    private void excludesProperties(List<List<EntityDescriptor>> all) {

        excludes = excludes.stream().map(String::trim).collect(Collectors.toSet());

        all.stream()
                .flatMap(Collection::stream)
                .forEach(it -> it.getFields().removeIf(f -> excludes.contains(f.getName())));
    }

    public Map<String, String> streamConvert(Set<String> properties) {
        return properties.stream()
                .filter(StringUtils::isNotEmpty)
                .map(it -> it.split("="))
                .filter(it -> {
                    if (it.length == 1) {
                        logMessage("Invalid entry:" + it[0]);
                    }
                    return true;
                })
                .collect(
                        Collectors.toMap(
                                it -> it[0].trim(),
                                it -> it[1].trim(),
                                (prev, next) -> next, HashMap::new
                        ));
    }

    void logMessage(String message) {

        Log log = getLog();

        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }
}
