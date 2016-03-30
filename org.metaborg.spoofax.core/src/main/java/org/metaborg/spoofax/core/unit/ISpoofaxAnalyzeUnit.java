package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.scopegraph.IScopeGraph;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Spoofax-specific extension of an {@link IAnalyzeUnit} that adds an {@link IStrategoTerm} AST.
 */
public interface ISpoofaxAnalyzeUnit extends IAnalyzeUnit {
    /**
     * @return True if the analyzer that created this unit attempted to make an AST. False otherwise.
     */
    boolean hasAst();

    /**
     * @return Analyzed AST, or null if {@link #valid()} or {@link #hasAst()} returns false. If {@link #success()}
     *         returns false, this AST may still represent an error recovered AST.
     */
    @Nullable IStrategoTerm ast();


    /**
     * @return True if the analyzer that created this unit attempted to make a scope graph. False otherwise.
     */
    boolean hasScopeGraph();

    /**
     * @return Scope graph, or null if {@link #valid()} or {@link #hasScopeGraph()} returns false. If {@link #success()}
     *         returns false, this scope graph may still represent an error recovered scope graph.
     */
    @Nullable IScopeGraph scopeGraph();


    /**
     * {@inheritDoc}
     */
    ISpoofaxParseUnit input();
}
