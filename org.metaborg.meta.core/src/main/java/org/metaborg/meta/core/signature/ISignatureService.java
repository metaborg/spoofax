package org.metaborg.meta.core.signature;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;

public interface ISignatureService {
    Iterable<Signature> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access);

    Iterable<Signature> get(ILanguageSpec languageSpec);

    Iterable<Signature> get(ILanguageImpl languageSpec);

    Iterable<Signature> get(ILanguageComponent languageSpec);
}
