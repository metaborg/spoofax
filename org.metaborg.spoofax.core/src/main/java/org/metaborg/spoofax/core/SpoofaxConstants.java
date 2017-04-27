package org.metaborg.spoofax.core;

import static org.metaborg.core.MetaborgConstants.METABORG_GROUP_ID;

import java.util.Set;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageName;

import com.google.common.collect.ImmutableSet;

public class SpoofaxConstants {
    public static final LanguageName LANG_ATERM_ID =
            new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.aterm");

    public static final LanguageName LANG_SDF_ID = new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.sdf");

    public static final LanguageName LANG_STRATEGO_ID =
            new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.stratego");

    public static final LanguageName LANG_ESV_ID = new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.esv");

    public static final LanguageName LANG_SDF3_ID =
            new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.template");

    public static final LanguageName LANG_NABL_ID = new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.nabl");

    public static final LanguageName LANG_TS_ID = new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.ts");

    public static final LanguageName LANG_DYNSEM_ID =
            new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.dynsem");

    public static final LanguageName LANG_SPT_ID = new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lang.spt");

    public static final LanguageName LIB_ANALYSIS_ID =
            new LanguageName(METABORG_GROUP_ID, "org.metaborg.meta.lib.analysis");


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
    public static final String DIR_TESTCLASSES = DIR_OUTPUT + "/test-classes";
    public static final String DIR_STR_JAVA_CLASSES = DIR_CLASSES + "/trans";


    public static boolean isMetaLanguage(ILanguageImpl language) {
        return isMetaLanguage(language.id().name());
    }

    public static boolean isMetaLanguage(LanguageName name) {
        return META_LANG_IDS.contains(name);
    }


    // @formatter:off
    public static final Set<LanguageName> META_LANG_IDS = ImmutableSet.<LanguageName>builder()
        .add(LANG_ATERM_ID)
        .add(LANG_SDF_ID)
        .add(LANG_STRATEGO_ID)
        .add(LANG_ESV_ID)
        .add(LANG_SDF3_ID)
        .add(LANG_NABL_ID)
        .add(LANG_TS_ID)
        .add(LANG_DYNSEM_ID)
        .add(LIB_ANALYSIS_ID)
        .build();
    // @formatter:on


    private SpoofaxConstants() {
    }
}
