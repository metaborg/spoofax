package org.metaborg.spoofax.core.processing;

import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.processing.ProcessorRunner;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

import com.google.inject.Inject;

/**
 * Typedef class for {@link ProcessorRunner} with Spoofax interfaces.
 */
public class SpoofaxProcessorRunner
    extends ProcessorRunner<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>
    implements ISpoofaxProcessorRunner {
    @Inject public SpoofaxProcessorRunner(ISpoofaxProcessor processor, ILanguageService languageService) {
        super(processor, languageService);
    }
}
