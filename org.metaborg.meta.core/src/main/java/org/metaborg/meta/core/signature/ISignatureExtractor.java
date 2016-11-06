package org.metaborg.meta.core.signature;

import java.io.IOException;
import java.util.Collection;

import org.metaborg.core.syntax.ParseException;
import org.metaborg.meta.core.project.ILanguageSpec;

public interface ISignatureExtractor {
    Collection<Signature> extract(ILanguageSpec languageSpec) throws IOException, ParseException;
}
