package org.metaborg.spoofax.core.build;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

import java.util.Collection;

import org.metaborg.core.config.AllLangSource;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.LangSource;
import org.metaborg.core.language.LanguageName;

import com.google.common.collect.Lists;

public class SpoofaxDefaultSources {
    private static final String ROOT = ".";

    public static final Collection<ISourceConfig> DEFAULT_SPOOFAX_SOURCES = Lists.newArrayList();

    static {
        for(LanguageName metaLang : META_LANG_IDS) {
            DEFAULT_SPOOFAX_SOURCES.add(new LangSource(metaLang, DIR_LIB));
            DEFAULT_SPOOFAX_SOURCES.add(new LangSource(metaLang, DIR_SRCGEN));
        }
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_SDF_ID, DIR_SYNTAX));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_STRATEGO_ID, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_ESV_ID, DIR_EDITOR));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_SDF3_ID, DIR_SYNTAX));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_NABL_ID, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_TS_ID, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_DYNSEM_ID, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new AllLangSource(ROOT));
    }

}