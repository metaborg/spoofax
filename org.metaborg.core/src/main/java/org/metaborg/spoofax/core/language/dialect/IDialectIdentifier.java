package org.metaborg.spoofax.core.language.dialect;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;

public interface IDialectIdentifier {
    /**
     * Attempts to identify the dialect of given resource.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified dialect, or null if dialect could not be identified.
     * @throws SpoofaxException
     *             When unable to determine if resource requires a dialect.
     * @throws SpoofaxException
     *             When resource requires a dialect that is not available.
     */
    public abstract @Nullable ILanguage identify(FileObject resource) throws SpoofaxException;

    /**
     * Checks if given resource is of given dialect.
     * 
     * @param resource
     *            Resource to check.
     * @param dialect
     *            Dialect to check against.
     * @return True if resource is of given dialect, false otherwise.
     * @throws SpoofaxException
     *             When unable to determine if resource requires a dialect.
     * @throws SpoofaxException
     *             When resource requires a dialect that is not available.
     */
    public abstract boolean identify(FileObject resource, ILanguage dialect) throws SpoofaxException;
}
