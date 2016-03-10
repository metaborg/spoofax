package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.IAnalyzeUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Spoofax-specific extension of an {@link IAnalyzeUnit} that adds an {@link IStrategoTerm} AST.
 */
public interface ISpoofaxAnalyzeUnit extends IAnalyzeUnit {
    /**
     * @return Analyzed AST, or null if {@link #valid()} return false. If {@link #success()} returns false, this AST may
     *         still represent an error recovered AST.
     */
    @Nullable IStrategoTerm ast();

    /**
     * {@inheritDoc}
     */
    ISpoofaxParseUnit input();
}
