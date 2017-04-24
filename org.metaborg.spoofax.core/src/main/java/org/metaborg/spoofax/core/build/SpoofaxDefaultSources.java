package org.metaborg.spoofax.core.build;

import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_EDITOR;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_LIB;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_SRCGEN;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_SYNTAX;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_TRANS;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_DYNSEM_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_ESV_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_NABL_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF3_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_STRATEGO_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_TS_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.META_LANG_NAMES;

import java.util.Collection;

import org.metaborg.core.config.GenericSource;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.LangSource;

import com.google.common.collect.Lists;

public class SpoofaxDefaultSources {
    private static final String ROOT = ".";

    public static final Collection<ISourceConfig> DEFAULT_SPOOFAX_SOURCES = Lists.newArrayList();

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
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_TS_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new LangSource(LANG_DYNSEM_NAME, DIR_TRANS));
        DEFAULT_SPOOFAX_SOURCES.add(new GenericSource(ROOT));
    }

}