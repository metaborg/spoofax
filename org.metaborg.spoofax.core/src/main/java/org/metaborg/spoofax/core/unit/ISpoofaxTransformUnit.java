package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.core.unit.IUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Spoofax-specific extension of an {@link ITransformUnit} that adds an {@link String} AST.
 * 
 * @param <I>
 *            Type of input unit.
 */
public interface ISpoofaxTransformUnit<I extends IUnit> extends ITransformUnit<I> {
    /**
     * @return Transformed AST, or null if {@link #valid()} return false. If {@link #success()} returns false, this AST
     *         may still represent an error recovered AST.
     */
    @Nullable IStrategoTerm ast();
    
    Iterable<? extends ISpoofaxTransformOutput> outputs();

}
