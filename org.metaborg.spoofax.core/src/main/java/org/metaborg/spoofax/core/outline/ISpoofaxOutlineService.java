package org.metaborg.spoofax.core.outline;

import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.tracing.IResolverService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IResolverService} with Spoofax interfaces.
 */
public interface ISpoofaxOutlineService extends IOutlineService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit> {

}
