package org.metaborg.core.config;

import java.util.Collection;

import org.metaborg.core.language.LanguageIdentifier;

public interface IProjectConfig {
    /**
     * Gets the version of MetaBorg tooling to use.
     * 
     * @return The version of MetaBorg tooling to use.
     */
    String metaborgVersion();
    
    /**
     * Gets the compile dependencies.
     *
     * @return The compile dependency identifiers.
     */
    Collection<LanguageIdentifier> compileDeps();

    /**
     * Gets the source dependencies.
     *
     * @return The source dependency identifiers.
     */
    Collection<LanguageIdentifier> sourceDeps();
    
    /**
     * Gets the Java dependencies.
     *
     * @return The Java dependency identifiers.
     */
    Collection<LanguageIdentifier> javaDeps();
}
