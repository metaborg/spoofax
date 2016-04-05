package org.metaborg.core.language.dialect;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for identifying the dialect of a resource.
 */
public interface IDialectIdentifier {
    /**
     * Attempts to identify the dialect of given resource.
     * 
     * @param resource
     *            Resource to identify.
     * @return Identified dialect, or null if dialect could not be identified.
     * @throws MetaborgException
     *             When unable to determine if resource requires a dialect.
     * @throws MetaborgException
     *             When resource requires a dialect that is not available.
     */
    @Nullable IdentifiedDialect identify(FileObject resource) throws MetaborgException;

    /**
     * Checks if given resource is of given dialect.
     * 
     * @param resource
     *            Resource to check.
     * @param dialect
     *            Dialect to check against.
     * @return True if resource is of given dialect, false otherwise.
     * @throws MetaborgException
     *             When unable to determine if resource requires a dialect.
     * @throws MetaborgException
     *             When resource requires a dialect that is not available.
     */
    boolean identify(FileObject resource, ILanguageImpl dialect) throws MetaborgException;
}
