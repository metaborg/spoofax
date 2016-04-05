package org.metaborg.spoofax.core.style;

import org.metaborg.core.style.ICategorizerService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Typedef interface for {@link ICategorizerService} with Spoofax interfaces.
 */
public interface ISpoofaxCategorizerService extends ICategorizerService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, IStrategoTerm> {

}
