package org.metaborg.spoofax.core.build;

import org.metaborg.core.build.IBuilder;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

/**
 * Typedef interface for {@link IBuilder} with Spoofax interfaces.
 */
public interface ISpoofaxBuilder extends IBuilder<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<?>> {

}
