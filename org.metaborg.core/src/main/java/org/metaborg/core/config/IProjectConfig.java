package org.metaborg.core.config;

import java.util.Collection;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.util.config.NaBL2Config;

public interface IProjectConfig {
    /**
     * @return Version of MetaBorg tooling to use.
     */
    String metaborgVersion();

    /**
     * @return Compile dependency identifiers.
     */
    Collection<LanguageIdentifier> compileDeps();

    /**
     * @return Source dependency identifiers.
     */
    Collection<LanguageIdentifier> sourceDeps();

    /**
     * @return Java dependency identifiers.
     */
    Collection<LanguageIdentifier> javaDeps();


    /**
     * @return Whether typesmart dynamic analysis is enabled.
     */
    boolean typesmart();

    /**
     * @return NaBL2 configuration.
     */
    NaBL2Config nabl2Config();
}
