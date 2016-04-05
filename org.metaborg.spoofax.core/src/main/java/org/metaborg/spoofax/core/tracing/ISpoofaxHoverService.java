package org.metaborg.spoofax.core.tracing;

import org.metaborg.core.tracing.IHoverService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IHoverService} with Spoofax interfaces.
 */
public interface ISpoofaxHoverService extends IHoverService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit> {

}
