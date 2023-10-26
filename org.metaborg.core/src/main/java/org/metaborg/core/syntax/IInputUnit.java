package org.metaborg.core.syntax;

import jakarta.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.unit.IUnit;

/**
 * Unit representing a textual source file of a certain language implementation. An input unit can be passed to the
 * {@link ISyntaxService} to parse the source file into a {@link IParseUnit}.
 */
public interface IInputUnit extends IUnit {
    /**
     * @return Text of the source file.
     */
    String text();

    /**
     * @return Language implementation of the source file.
     */
    ILanguageImpl langImpl();

    /**
     * @return Dialect of the source file, or null if it has no dialect.
     */
    @Nullable ILanguageImpl dialect();
}
