package org.metaborg.spoofax.core.unit;

import jakarta.annotation.Nullable;

import org.metaborg.core.syntax.IParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Spoofax-specific extension of a parse unit that adds an {@link IStrategoTerm} AST.
 */
public interface ISpoofaxParseUnit extends IParseUnit {
    /**
     * @return Parsed AST, or null if {@link #valid()} return false. If {@link #success()} returns false, this AST may
     *         still represent an error recovered AST.
     */
    @Nullable IStrategoTerm ast();

    /**
     * {@inheritDoc}
     */
    ISpoofaxInputUnit input();
}
