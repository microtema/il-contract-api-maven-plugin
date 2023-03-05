package de.microtema.maven.plugin.contract.util;

import java.io.File;
import java.util.Objects;

public class MojoUtil {

    public static String getPackageDirectory(String packageName) {

        String packageDirectory = packageName.replaceAll("\\.", File.separator);

        if (!packageDirectory.endsWith(File.separator)) {
            packageDirectory = packageDirectory + File.separator;
        }

        return packageDirectory;
    }

    public static String cleanUp(String str) {

        return str.replaceAll(File.separator, "");
    }

    public static String getTemplateType(File baseDir) {

        File[] files = baseDir.listFiles((dir, name) -> name.toLowerCase().contains(".md") || name.toLowerCase().contains(".adoc"));

        if (Objects.isNull(files)) {
            return null;
        }

        return files[0].getName();
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
