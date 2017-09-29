package org.metaborg.core.config;

import java.util.Collection;

import org.metaborg.core.language.LanguageIdentifier;

public interface IProjectConfig {
    /**
     * @return Version of MetaBorg tooling to use.
     */
    String metaborgVersion();

    /**
     * Gets the file sources.
     *
     * @return The file sources.
     */
    Collection<ISourceConfig> sources();

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

}