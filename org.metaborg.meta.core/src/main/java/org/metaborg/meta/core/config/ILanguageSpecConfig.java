package org.metaborg.meta.core.config;

import java.util.Collection;

import org.metaborg.core.config.ILanguageComponentConfig;

/**
 * Configuration of a language specification at build time, an extension of the {@link ILanguageComponentConfig} runtime
 * configuration.
 *
 * To create a new instance of this interface, use an {@link ILanguageSpecConfigBuilder} interface.
 */
public interface ILanguageSpecConfig extends ILanguageComponentConfig {
    /**
     * Gets the version of MetaBorg tooling to use.
     * 
     * @return The version of MetaBorg tooling to use.
     */
    String metaborgVersion();

    /**
     * Gets a sequence of languages whose errors are ignored.
     *
     * @return The pardoned languages.
     */
    Collection<String> pardonedLanguages();
}
