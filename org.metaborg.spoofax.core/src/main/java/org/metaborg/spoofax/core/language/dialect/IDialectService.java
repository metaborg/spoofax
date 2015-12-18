package org.metaborg.spoofax.core.language.dialect;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacet;

public interface IDialectService {
    /**
     * Returns if dialect with given name exists.
     * 
     * @param name
     *            Name of the dialect to check.
     * @return True if dialect exists, false if not.
     */
    public abstract boolean hasDialect(String name);

    /**
     * Gets dialect language with given name.
     * 
     * @param name
     *            Name of the dialect to get.
     * @return Dialect language with given name, or null if it does not exist.
     */
    public abstract @Nullable ILanguage getDialect(String name);

    /**
     * Gets all dialects for given base language
     * 
     * @param base
     *            Base language of the dialects.
     * @return Dialects for given base language.
     */
    public abstract Iterable<ILanguage> getDialects(ILanguage base);

    /**
     * Gets the base language for given dialect.
     * 
     * @param dialect
     *            Dialect to get base language for.
     * @return Base language of given dialect, or null if given language is not a dialect or the dialect has been
     *         removed.
     */
    public abstract @Nullable ILanguage getBase(ILanguage dialect);

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
     * @throws SpoofaxRuntimeException
     *             When dialect with given name already exists.
     */
    public abstract ILanguage add(String name, FileObject location, ILanguage base, ILanguageFacet syntaxFacet);

    /**
     * Updates dialect of given name, with a new parser facet.
     * 
     * @param name
     *            Name of the dialect to update.
     * @param parserFacet
     *            Parser facet to update the dialect with.
     * @return Updated dialect language.
     * @throws SpoofaxRuntimeException
     *             When dialect with given name does not exist.
     */
    public abstract ILanguage update(String name, ILanguageFacet parserFacet);

    /**
     * Updates all dialects based on {@code oldBase} to be based on {@code newBase}. Does nothing if there were no
     * dialects based on {@code oldBase}.
     * 
     * @param oldBase
     *            Old base language to update.
     * @param newBase
     *            New base language to update.
     * @return Updated dialect languages.
     */
    public abstract Iterable<ILanguage> update(ILanguage oldBase, ILanguage newBase);

    /**
     * Removes dialect with given name.
     * 
     * @param name
     *            Name of the dialect to remove.
     * @throws SpoofaxRuntimeException
     *             When dialect with given name does not exist.
     * @return Removed dialect language.
     */
    public abstract ILanguage remove(String name);

    /**
     * Removes all dialects based on given language. Does nothing if there are no dialects of that language.
     * 
     * @param base
     *            Base language to remove all dialects for.
     * @return Removed dialect languages.
     */
    public abstract Iterable<ILanguage> remove(ILanguage base);
}
