package org.metaborg.meta.core.project;

import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.config.ILanguageSpecConfig;

/**
 * A language specification project.
 *
 * A language specification is compiled into a language implementation, which can then be used in other projects.
 */
public interface ILanguageSpec extends IProject {
    /**
     * Gets the configuration of the language specification.
     * 
     * @return Configuration of the language specification.
     */
    ILanguageSpecConfig config();
    
    /**
     * Gets the paths of the language specification.
     * 
     * @return Paths of the language specification.
     */
    ILanguageSpecPaths paths();
}
