package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.analysis.IAnalyzer;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * Typedef interface for {@link IAnalyzer} with Spoofax interfaces.
 */
public interface ISpoofaxAnalyzer extends IAnalyzer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate> {

}
