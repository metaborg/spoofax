package org.metaborg.core.build.paths;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;

/**
 * Interface for service that returns source/include paths/files for languages.
 */
public interface ILanguagePathService {
    /**
     * Gets source paths in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get source paths relative to.
     * @param languageName
     *            Name of the language to get source paths for.
     * @return Source paths.
     */
    public Iterable<FileObject> sourcePaths(ILanguageSpec languageSpec, String languageName);

    /**
     * Gets include paths in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get include paths relative to.
     * @param languageName
     *            Name of the language to get include paths for.
     * @return Include paths.
     */
    public Iterable<FileObject> includePaths(ILanguageSpec languageSpec, String languageName);

    /**
     * Gets source and include paths in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get source and include paths relative to.
     * @param languageName
     *            Name of the language to get source and include paths for.
     * @return Source and include paths.
     */
    public Iterable<FileObject> sourceAndIncludePaths(ILanguageSpec languageSpec, String languageName);


    /**
     * Gets source files in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get source files relative to.
     * @param language
     *            Language to get source files for.
     * @return Identified source files.
     */
    public Iterable<IdentifiedResource> sourceFiles(ILanguageSpec languageSpec, ILanguageImpl language);

    /**
     * Gets include files in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get include files relative to.
     * @param language
     *            Language to get include files for.
     * @return Identified include files.
     */
    public Iterable<IdentifiedResource> includeFiles(ILanguageSpec languageSpec, ILanguageImpl language);

    /**
     * Gets source and include files in given language specification, for given language.
     * 
     * @param languageSpec
     *            Language specification to get source and include files relative to.
     * @param language
     *            Language to get source and include files for.
     * @return Identified source and include files.
     */
    public Iterable<IdentifiedResource> sourceAndIncludeFiles(ILanguageSpec languageSpec, ILanguageImpl language);


    /**
     * Gets source/include files given source/include paths.
     * 
     * @param paths
     *            Source and/or include paths to get files for.
     * @param language
     *            Language to get source and include files for.
     * @return Identified source and include files.
     */
    public Iterable<IdentifiedResource> toFiles(Iterable<FileObject> paths, ILanguageImpl language);
}
