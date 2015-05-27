package org.metaborg.spoofax.core.processing;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link IParseResultProcessor} with {@link IStrategoTerm}.
 */
public interface ISpoofaxParseResultProcessor extends IParseResultProcessor<IStrategoTerm>,
    ISpoofaxParseResultRequester, ISpoofaxParseResultUpdater {

}
