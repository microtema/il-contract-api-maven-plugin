package de.microtema.maven.plugin.contract.java.template;

import java.io.File;

public class FileUtil {

    public static String getPackageDirectory(String packageName) {

        String packageDirectory = packageName.replaceAll("\\.", File.separator);

        if (!packageDirectory.endsWith(File.separator)) {
            packageDirectory = packageDirectory + File.separator;
        }

        return packageDirectory;
    }

    public static String lineSeparator(int lines) {

        int index = 0;

        String str = "";

        while (index++ < lines) {
            str += System.lineSeparator();
        }

        return str;
    }
}
