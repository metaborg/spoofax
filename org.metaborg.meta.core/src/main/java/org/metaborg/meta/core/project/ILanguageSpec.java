package org.metaborg.meta.core.project;

import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.config.ILanguageSpecConfig;

/**
 * A language specification project. A language specification is compiled into a language implementation.
 */
public interface ILanguageSpec extends IProject {
    /**
     * Gets the configuration of the language specification. The configuration is read only once when this class is
     * instantiated. To get a new configuration, get a new instance of this class.
     * 
     * @return Configuration of the language specification.
     */
    ILanguageSpecConfig config();
}
