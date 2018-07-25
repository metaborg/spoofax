package org.metaborg.spoofax.core.semantic_provider;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.spoofax.core.user_definable.IHoverText;
import org.metaborg.spoofax.core.user_definable.IOutliner;
import org.metaborg.spoofax.core.user_definable.IResolver;
import org.metaborg.spoofax.core.user_definable.ITransformer;

public interface ISemanticProviderService {
    IOutliner outliner(ILanguageComponent component, String className) throws MetaborgException;
    IResolver resolver(ILanguageComponent component, String className) throws MetaborgException;
    IHoverText hoverer(ILanguageComponent component, String className) throws MetaborgException;
    ITransformer transformer(ILanguageComponent component, String className) throws MetaborgException;
    <T> T loadClass(ILanguageComponent component, String className, Class<T> expectedType) throws MetaborgException;
}
