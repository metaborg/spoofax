package org.metaborg.spoofax.core.unit;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IInputUnitService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;

public interface ISpoofaxInputUnitService extends IInputUnitService<ISpoofaxInputUnit> {
    ISpoofaxInputUnit inputUnit(FileObject source, String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config);

    ISpoofaxInputUnit inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect,
        @Nullable JSGLRParserConfiguration config);
}
