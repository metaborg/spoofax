package org.metaborg.spoofax.core.dynamicclassloading.api;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.metaborg.core.context.IContext;
import org.metaborg.core.outline.IOutlineNode;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.dynamicclassloading.IBuilderInput;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IOutliner {
    @Nullable Iterable<IOutlineNode> createOutline(IContext env, IBuilderInput input, Function<IStrategoTerm, ISourceRegion> region);
}
