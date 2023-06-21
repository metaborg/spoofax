package org.metaborg.spoofax.core.build;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

import java.util.ArrayList;
import java.util.Collection;

import org.metaborg.core.config.AllLangSource;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.LangSource;

public class SpoofaxDefaultSources {
    private static final String ROOT = ".";

    public static final Collection<ISourceConfig> DEFAULT_SPOOFAX_SOURCES = new ArrayList<>();

    static {
        for(String metaLang : META_LANG_NAMES) {
            DEFAULT_SPOOFAX_SOURCES.add(new LangSource(metaLang, DIR_LIB));
            DEFAULT_SPOOFAX_SOURCES.add(new LangSource(metaLang, DIR_SRCGEN));
        }
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_SDF_NAME, DIR_SYNTAX));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_STRATEGO_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_ESV_NAME, DIR_EDITOR));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_SDF3_NAME, DIR_SYNTAX));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_NABL_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_NABL2_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_STATIX_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_FLOWSPEC_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_TS_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_DYNSEM_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new AllLangSource(ROOT));
    }

}