package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnit;

public interface IInputUnit extends IUnit {
    String text();

    ILanguageImpl langImpl();

    @Nullable ILanguageImpl dialect();
}
