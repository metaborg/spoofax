package org.metaborg.spoofax.core.processing;

import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.processing.BlockingProcessor;
import org.metaborg.core.processing.ILanguageChangeProcessor;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

import com.google.inject.Inject;

/**
 * Typedef class for {@link BlockingProcessor} with Spoofax interfaces.
 */
public class SpoofaxBlockingProcessor extends
    BlockingProcessor<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>
    implements ISpoofaxProcessor {
    @Inject public SpoofaxBlockingProcessor(IDialectProcessor dialectProcessor, ISpoofaxBuilder builder,
        ILanguageChangeProcessor languageChangeProcessor) {
        super(dialectProcessor, builder, languageChangeProcessor);
    }
}
