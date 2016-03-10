package org.metaborg.spoofax.core.tracing;

import org.metaborg.core.tracing.IResolverService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IResolverService} with Spoofax interfaces.
 */
public interface ISpoofaxResolverService extends IResolverService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit> {

}
