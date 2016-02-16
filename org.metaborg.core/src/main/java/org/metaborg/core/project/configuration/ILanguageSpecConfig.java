package org.metaborg.core.project.configuration;

import java.io.Serializable;

/**
 * Configuration of a language specification at build time, an extension of the {@link ILanguageComponentConfig} runtime
 * configuration.
 *
 * To create a new instance of this interface, use an {@link ILanguageSpecConfigBuilder} interface.
 */
public interface ILanguageSpecConfig extends ILanguageComponentConfig, Serializable {
    /**
     * Gets the version of MetaBorg tooling to use.
     * 
     * @return The version of MetaBorg tooling to use.
     */
    String metaborgVersion();
}
