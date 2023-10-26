package org.metaborg.core.language.dialect;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageImpl;

public interface IDialectService {
    /**
     * Returns dialect name for the given dialect.
     * 
     * @param dialect
     *            The dialect.
     * @return the name of the dialect, null if it isn't a dialect.
     */
    @Nullable String dialectName(ILanguageImpl dialect);


    /**
     * Returns if dialect with given name exists.
     * 
     * @param name
     *            Name of the dialect to check.
     * @return True if dialect exists, false if not.
     */
    boolean hasDialect(String name);

    /**
     * Gets dialect language with given name.
     * 
     * @param name
     *            Name of the dialect to get.
     * @return Dialect language with given name, or null if it does not exist.
     */
    @Nullable ILanguageImpl getDialect(String name);

    /**
     * Gets all dialects for given base language
     * 
     * @param base
     *            Base language of the dialects.
     * @return Dialects for given base language.
     */
    Iterable<ILanguageImpl> getDialects(ILanguageImpl base);

    /**
     * Gets the base language for given dialect.
     * 
     * @param dialect
     *            Dialect to get base language for.
     * @return Base language of given dialect, or null if given language is not a dialect or the dialect has been
     *         removed.
     */
    @Nullable ILanguageImpl getBase(ILanguageImpl dialect);

    /**
     * Creates a new dialect language for given base language and parser facet. The dialect replaces the parser facet,
     * and retains all other facets of the base language.
     * 
     * @param name
     *            Name of the new dialect language.
     * @param location
     *            Location of the new dialect language.
     * @param base
     *            Base language of the dialect.
     * @param syntaxFacet
     *            Syntax facet to replace the base language's syntax facet with.
     * @return Created dialect language.
     * @throws MetaborgRuntimeException
     *             When dialect with given name already exists.
     */
    ILanguageImpl add(String name, FileObject location, ILanguageImpl base, IFacet syntaxFacet);

    /**
     * Updates dialect of given name, with a new parser facet.
     * 
     * @param name
     *            Name of the dialect to update.
     * @param parserFacet
     *            Parser facet to update the dialect with.
     * @return Updated dialect language.
     * @throws MetaborgRuntimeException
     *             When dialect with given name does not exist.
     */
    ILanguageImpl update(String name, IFacet parserFacet);

    /**
     * Updates all dialects based on {@code base}. Does nothing if there were no dialects based on {@code base}.
     * 
     * @param base
     *            Base language for which to update dialects.
     * @return Updated dialect languages.
     */
    Iterable<ILanguageImpl> update(ILanguageImpl base);

    /**
     * Removes dialect with given name.
     * 
     * @param name
     *            Name of the dialect to remove.
     * @throws MetaborgRuntimeException
     *             When dialect with given name does not exist.
     * @return Removed dialect language.
     */
    ILanguageImpl remove(String name);

    /**
     * Removes all dialects based on given language. Does nothing if there are no dialects of that language.
     * 
     * @param base
     *            Base language to remove all dialects for.
     * @return Removed dialect languages.
     */
    Iterable<ILanguageImpl> remove(ILanguageImpl base);
}
