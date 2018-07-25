package org.metaborg.spoofax.core.transform;

import org.metaborg.core.transform.ITransformer;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link ITransformer} with Spoofax interfaces.
 */
public interface ISpoofaxTransformer extends
    ITransformer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> {

}
