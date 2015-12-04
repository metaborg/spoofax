package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * A language specification.
 *
 * A language specification is built into a language implementation,
 * which can then be used in other projects.
 */
public interface ILanguageSpec {

    /**
     * Gets the location of the root folder of the language specification.
     *
     * @return Location of the root folder.
     */
    FileObject location();
}
