package org.metaborg.core.unit;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IInputUnit;

public interface IInputUnitService<I extends IInputUnit> {
    I inputUnit(FileObject source, String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I inputUnit(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInputUnit(FileObject source, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInputUnit(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);
}
