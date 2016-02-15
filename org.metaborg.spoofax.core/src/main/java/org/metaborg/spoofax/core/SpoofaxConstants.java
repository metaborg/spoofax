package org.metaborg.spoofax.core;

import java.util.Set;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.ImmutableSet;

public class SpoofaxConstants {
    public static final String LANG_ATERM_ID = "org.metaborg.meta.lang.aterm";
    public static final String LANG_ATERM_NAME = "ATerm";

    public static final String LANG_SDF_ID = "org.metaborg.meta.lang.sdf";
    public static final String LANG_SDF_NAME = "SDF";

    public static final String LANG_STRATEGO_ID = "org.metaborg.meta.lang.stratego";
    public static final String LANG_STRATEGO_NAME = "Stratego-Sugar";

    public static final String LANG_ESV_ID = "org.metaborg.meta.lang.esv";
    public static final String LANG_ESV_NAME = "EditorService";

    public static final String LANG_SDF3_ID = "org.metaborg.meta.lang.template";
    public static final String LANG_SDF3_NAME = "TemplateLang";

    public static final String LANG_NABL_ID = "org.metaborg.meta.lang.nabl";
    public static final String LANG_NABL_NAME = "NameBindingLanguage";

    public static final String LANG_TS_ID = "org.metaborg.meta.lang.ts";
    public static final String LANG_TS_NAME = "TypeSystemLanguage";

    public static final String LANG_DYNSEM_ID = "org.metaborg.meta.lang.dynsem";
    public static final String LANG_DYNSEM_NAME = "ds";

    public static final String LIB_ANALYSIS_ID = "org.metaborg.meta.lib.analysis";
    public static final String LIB_ANALYSIS_NAME = "runtime-libraries";

    public static final String LIB_ANALYSIS2_ID = "org.metaborg.meta.lib.analysis2";
    public static final String LIB_ANALYSIS2_NAME = "libanalysis2";


    public static final String DIR_CACHE = ".cache";
    public static final String DIR_EDITOR = "editor";
    public static final String DIR_SYNTAX = "syntax";
    public static final String DIR_TRANS = "trans";
    public static final String DIR_INCLUDE = "include";
    public static final String DIR_BUILD = DIR_INCLUDE + "/build";
    public static final String DIR_LIB = "lib";
    public static final String DIR_ICONS = "icons";
    public static final String DIR_SRCGEN = "src-gen";
    public static final String DIR_SRCGEN_SYNTAX = DIR_SRCGEN + "/syntax";
    public static final String DIR_STR_JAVA = DIR_EDITOR + "/java";
    public static final String DIR_STR_JAVA_TRANS = DIR_STR_JAVA + "/trans";
    public static final String DIR_OUTPUT = "target";
    public static final String DIR_CLASSES = DIR_OUTPUT + "/classes";
    public static final String DIR_STR_JAVA_CLASSES = DIR_CLASSES + "/trans";


    public static boolean isMetaLanguage(ILanguageImpl language) {
        return isMetaLanguage(language.id());
    }

    public static boolean isMetaLanguage(LanguageIdentifier identifier) {
        return isMetaLanguage(identifier.groupId, identifier.id);
    }

    public static boolean isMetaLanguage(String groupId, String id) {
        return groupId.equals(MetaborgConstants.METABORG_GROUP_ID) && META_LANG_IDS.contains(id);
    }

    public static boolean isMetaLanguage(String name) {
        return META_LANG_NAMES.contains(name);
    }

    // @formatter:off
    public static final Set<String> META_LANG_IDS = ImmutableSet.<String>builder()
        .add(LANG_ATERM_ID)
        .add(LANG_SDF_ID)
        .add(LANG_STRATEGO_ID)
        .add(LANG_ESV_ID)
        .add(LANG_SDF3_ID)
        .add(LANG_NABL_ID)
        .add(LANG_TS_ID)
        .add(LANG_DYNSEM_ID)
        .add(LIB_ANALYSIS_ID)
        .add(LIB_ANALYSIS2_ID)
        .build();
    // @formatter:on

    // @formatter:off
    public static final Set<String> META_LANG_NAMES = ImmutableSet.<String>builder()
        .add(LANG_ATERM_NAME)
        .add(LANG_SDF_NAME)
        .add(LANG_STRATEGO_NAME)
        .add(LANG_ESV_NAME)
        .add(LANG_SDF3_NAME)
        .add(LANG_NABL_NAME)
        .add(LANG_TS_NAME)
        .add(LANG_DYNSEM_NAME)
        .add(LIB_ANALYSIS_NAME)
        .add(LIB_ANALYSIS2_NAME)
        .build();
    // @formatter:on


    private SpoofaxConstants() {
    }
}
