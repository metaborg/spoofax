package org.metaborg.meta.core.signature;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;

public interface ISigService {
    Iterable<ISig> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access);

    Iterable<ISig> get(ILanguageSpec languageSpec);

    Iterable<ISig> get(ILanguageImpl languageSpec);

    Iterable<ISig> get(ILanguageComponent languageSpec);
}
