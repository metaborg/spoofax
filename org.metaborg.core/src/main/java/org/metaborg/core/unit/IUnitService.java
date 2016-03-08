package org.metaborg.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IInputUnit;

public interface IUnitService<I extends IInputUnit> {
    I input(FileObject source, String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I input(String text, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInput(FileObject source, ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);

    I emptyInput(ILanguageImpl langImpl, @Nullable ILanguageImpl dialect);
}
