package org.metaborg.spoofax.core.user_definable;

import org.metaborg.core.context.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IHoverText {
    String createHoverText(IContext env, IStrategoTerm region);
}
