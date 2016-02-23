package org.metaborg.core.config;

/**
 * Configuration for a language that generates source files in a directory.
 */
public interface IGenerateConfig {
    /**
     * @return Name of the language for which source files are generated.
     */
    String languageName();

    /**
     * @return Directory to which source files are generated, relative to a project root.
     */
    String directory();
}
