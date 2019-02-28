package org.metaborg.spoofax.core.dynamicclassloading.api;

import org.metaborg.core.context.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IHoverText {
    String createHoverText(IContext env, IStrategoTerm region);
}
