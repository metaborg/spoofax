package org.metaborg.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;

/**
 * Interface for providers for source and include paths.
 */
public interface ILanguagePathProvider {
    /**
     * Gets source paths in given project, for given language.
     * 
     * @param project
     *            Project to get source paths relative to.
     * @param languageName
     *            Name of the language to get source paths for.
     * @return Source paths.
     * @throws MetaborgException
     *             When getting source paths fails unexpectedly.
     */
    Iterable<FileObject> sourcePaths(IProject project, String languageName) throws MetaborgException;

    /**
     * Gets include paths in given project, for given language.
     * 
     * @param project
     *            Project to get include paths relative to.
     * @param languageName
     *            Name of the language to get include paths for.
     * @return Include paths.
     * @throws MetaborgException
     *             When getting include paths fails unexpectedly.
     */
    Iterable<FileObject> includePaths(IProject project, String languageName) throws MetaborgException;
}
