package org.metaborg.spoofax.meta.core.project;

import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;

public interface ISpoofaxLanguageSpec extends ILanguageSpec {
    ISpoofaxLanguageSpecConfig config();

    ISpoofaxLanguageSpecPaths paths();
}
