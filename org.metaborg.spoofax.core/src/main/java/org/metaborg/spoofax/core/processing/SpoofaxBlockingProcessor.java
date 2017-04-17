package org.metaborg.spoofax.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.processing.BlockingProcessor;
import org.metaborg.core.processing.ILanguageChangeProcessor;
import org.metaborg.core.processing.ITask;
import org.metaborg.spoofax.core.build.ISpoofaxBuildOutput;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

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


    @SuppressWarnings("unchecked") @Override public ITask<ISpoofaxBuildOutput> build(BuildInput input,
        @Nullable IProgress progress, @Nullable ICancel cancel) {
        return (ITask<ISpoofaxBuildOutput>) super.build(input, progress, cancel);
    }
}
