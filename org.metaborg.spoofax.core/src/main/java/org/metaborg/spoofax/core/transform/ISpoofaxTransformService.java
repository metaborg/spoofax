package org.metaborg.spoofax.core.transform;

import org.metaborg.core.transform.ITransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link ITransformService} with Spoofax interfaces.
 */
public interface ISpoofaxTransformService extends
    ITransformService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>, ISpoofaxTransformAction> {

}
