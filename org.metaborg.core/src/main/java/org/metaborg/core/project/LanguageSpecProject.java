package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * A language specification project.
 */
public class LanguageSpecProject extends Project implements ILanguageSpec {

    /**
     * Initializes a new instance of the {@link LanguageSpecProject} class.
     *
     * @param location The project root location.
     */
    public LanguageSpecProject(final FileObject location) {
        super(location);
    }
}
