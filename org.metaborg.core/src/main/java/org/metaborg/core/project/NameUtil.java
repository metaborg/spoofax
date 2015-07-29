package org.metaborg.core.project;

import java.util.regex.Pattern;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

public class NameUtil {
    private static final Pattern PART = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
    private static final Pattern NAME = Pattern.compile(PART + "(-" + PART + ")*");
    private static final Pattern ID = Pattern.compile(NAME + "(\\." + NAME + ")*");


    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && NAME.matcher(name).matches();
    }

    public static boolean isValidId(String id) {
        return id != null && !id.isEmpty() && ID.matcher(id).matches();
    }

    public static boolean isValidVersion(String version) {
        return LanguageVersion.valid(version);
    }

    public static boolean isValidLanguageIdentifier(LanguageIdentifier identifier) {
        return isValidId(identifier.groupId) && isValidId(identifier.id);
    }

    public static boolean isValidFileExtension(String ext) {
        return PART.matcher(ext).matches();
    }

    public static String toJavaId(String id) {
        return id.replace('-', '_');
    }


    private NameUtil() {
    }
}
