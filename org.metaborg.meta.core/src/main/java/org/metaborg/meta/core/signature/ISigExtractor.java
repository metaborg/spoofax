package org.metaborg.meta.core.signature;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.core.syntax.ParseException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.util.file.IFileAccess;

public interface ISigExtractor {
    Collection<ISig> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access)
        throws IOException, ParseException;
}
