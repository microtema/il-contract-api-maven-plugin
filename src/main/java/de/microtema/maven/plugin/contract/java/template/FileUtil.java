package de.microtema.maven.plugin.contract.java.template;

import java.io.File;

public class FileUtil {

    public static String getPackageDirectory(String packageName){

        String packageDirectory = packageName.replaceAll("\\.", File.separator);

        if (!packageDirectory.endsWith(File.separator)) {
            packageDirectory = packageDirectory + File.separator;
        }

        return packageDirectory;
    }
}
