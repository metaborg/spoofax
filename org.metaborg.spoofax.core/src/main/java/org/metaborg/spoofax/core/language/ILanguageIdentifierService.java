package org.metaborg.spoofax.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface ILanguageIdentifierService {
    /**
     * Attempts to identify the active language of given file object.
     * 
     * @param file
     *            The file to identify.
     * @return Identified language, or null if language could not be identified.
     * @throws IllegalStateException
     *             when a resource can be identified to languages with different names.
     */
    public @Nullable ILanguage identify(FileObject file);

    /**
     * Attempts to identify the languages (with the same name) of given file object.
     * 
     * @param file
     *            The file to identify.
     * @return Identified languages.
     * @throws IllegalStateException
     *             when a resource can be identified to languages with different names.
     */
    public Iterable<ILanguage> identifyAll(FileObject file);
}
