package org.metaborg.spoofax.core.dynamicclassloading;

import java.util.List;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;

public interface IDynamicClassLoadingService {
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
    <T> List<T> loadClasses(ILanguageComponent component, Class<T> type) throws MetaborgException;
}
