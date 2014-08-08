package org.metaborg.spoofax.core.terms;

import org.metaborg.spoofax.core.language.ILanguage;
import org.spoofax.interpreter.terms.ITermFactory;

public interface ITermFactoryService {
    public ITermFactory get(ILanguage language);

    public ITermFactory getGeneric();
}
