package org.metaborg.spoofax.core.dynamicclassloading.api;

import org.metaborg.core.context.IContext;
import org.metaborg.core.tracing.ResolutionTarget;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IResolver {
    Iterable<ResolutionTarget> resolve(IContext env, IStrategoTerm region);
    interface Generated extends IResolver {}
}
