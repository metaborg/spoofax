package org.metaborg.spoofax.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface ILanguageIdentifierService {
    /**
     * Attempts to identify the active language of given file object.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified language, or null if language could not be identified.
     * @throws IllegalStateException
     *             when a resource can be identified to languages with different names.
     */
    public abstract @Nullable ILanguage identify(FileObject resource);

    /**
     * Checks if given resource is of given language.
     * 
     * @param resource
     *            Resource to check.
     * @param language
     *            Language to check against.
     * @return True if resource is of given language, false otherwise.
     */
    public abstract boolean identify(FileObject resource, ILanguage language);
}
