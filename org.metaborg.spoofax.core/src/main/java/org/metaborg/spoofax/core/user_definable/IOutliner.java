package org.metaborg.spoofax.core.user_definable;

import java.util.function.Function;

import org.metaborg.core.context.IContext;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.semantic_provider.IBuilderInput;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IOutliner {
    Iterable<IOutlineNode> createOutline(IContext env, IBuilderInput input, Function<IStrategoTerm, ISourceRegion> region);
}
