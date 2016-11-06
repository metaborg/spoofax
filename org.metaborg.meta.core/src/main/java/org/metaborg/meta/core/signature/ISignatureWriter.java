package org.metaborg.meta.core.signature;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;

public interface ISignatureWriter {
    void write(ILanguageSpec languageSpec, Iterable<Signature> signatures, @Nullable IFileAccess access)
        throws MetaborgException;
}
