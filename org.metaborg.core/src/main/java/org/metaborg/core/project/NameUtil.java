package org.metaborg.core.project;

public class NameUtil {
    public static boolean isValidFileExtension(String ext) {
        return !ext.contains(" ");
    }

    public static String toJavaId(String id) {
        return id.replace('-', '_');
    }
}
