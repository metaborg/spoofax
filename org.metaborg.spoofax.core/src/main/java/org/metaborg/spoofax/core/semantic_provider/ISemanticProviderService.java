package org.metaborg.spoofax.core.semantic_provider;

import java.util.Iterator;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.spoofax.core.user_definable.IHoverText;
import org.metaborg.spoofax.core.user_definable.IOutliner;
import org.metaborg.spoofax.core.user_definable.IResolver;
import org.metaborg.spoofax.core.user_definable.ITransformer;

public interface ISemanticProviderService {
    /**
     * Load the outliner from Spoofax or a provider jar of the language component
     * 
     * @param component
     *            language component that the outliner is in
     * @param className
     *            fully qualified class name of the outliner
     * @return the outliner, instantiated with its zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    IOutliner outliner(ILanguageComponent component, String className) throws MetaborgException;

    /**
     * Load the resolver from Spoofax or a provider jar of the language component
     * 
     * @param component
     *            language component that the resolver is in
     * @param className
     *            fully qualified class name of the resolver
     * @return the resolver, instantiated with its zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    IResolver resolver(ILanguageComponent component, String className) throws MetaborgException;

    /**
     * Load the hoverer from Spoofax or a provider jar of the language component
     * 
     * @param component
     *            language component that the hoverer is in
     * @param className
     *            fully qualified class name of the hoverer
     * @return the hoverer, instantiated with its zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    IHoverText hoverer(ILanguageComponent component, String className) throws MetaborgException;

    /**
     * Load the transformer from Spoofax or a provider jar of the language component
     * 
     * @param component
     *            language component that the transformer is in
     * @param className
     *            fully qualified class name of the transformer
     * @return the transformer, instantiated with its zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    ITransformer transformer(ILanguageComponent component, String className) throws MetaborgException;

    /**
     * Load a class from Spoofax or a provider jar of the language component
     * 
     * @param component
     *            language component that may have provider jars
     * @param className
     *            fully qualified class name
     * @param expectedType
     *            the (super)type the class, which it's cast to
     * @return the class instance, instantiated with its zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    <T> T loadClass(ILanguageComponent component, String className, Class<T> expectedType) throws MetaborgException;

    /**
     * Load all classes of a certain type from Spoofax and/or a provider jar of the language component
     * 
     * @param component
     *            language component that the transformer is in
     * @param type
     *            the (super)type the class, which it's cast to
     * @return a lazy iterator of class instances, instantiated with their zero-argument constructor
     * @throws MetaborgException
     *             on failure to find, instantiate, access, or cast the class
     */
    <T> Iterator<T> loadClasses(ILanguageComponent component, Class<T> type) throws MetaborgException;
}
