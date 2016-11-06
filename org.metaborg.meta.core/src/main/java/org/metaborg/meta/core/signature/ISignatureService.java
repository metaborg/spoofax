package org.metaborg.meta.core.signature;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.core.project.ILanguageSpec;

public interface ISignatureService {
    Iterable<Signature> get(ILanguageSpec languageSpec);

    Iterable<Signature> get(ILanguageImpl languageSpec);

    Iterable<Signature> get(ILanguageComponent languageSpec);


}
