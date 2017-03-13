package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

import com.google.inject.Inject;

import rx.functions.Func0;

/**
 * Processor implementation that uses {@link BlockingTask} as task implementation. Tasks execute and block when
 * scheduled.
 */
public class BlockingProcessor<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>>
    implements IProcessor<P, A, AU, T> {
    private final IDialectProcessor dialectProcessor;
    private final IBuilder<P, A, AU, T> builder;
    private final ILanguageChangeProcessor languageChangeProcessor;


    @Inject public BlockingProcessor(IDialectProcessor dialectProcessor, IBuilder<P, A, AU, T> builder,
        ILanguageChangeProcessor languageChangeProcessor) {
        this.dialectProcessor = dialectProcessor;
        this.builder = builder;
        this.languageChangeProcessor = languageChangeProcessor;
    }


    @Override public ITask<? extends IBuildOutput<P, A, AU, T>> build(final BuildInput input,
        final @Nullable IProgress progressReporter, final @Nullable ICancel cancellationToken) {
        return new BlockingTask<>(new Func0<IBuildOutput<P, A, AU, T>>() {
            @Override public IBuildOutput<P, A, AU, T> call() {
                final IProgress pr = progressReporter != null ? progressReporter : new NullProgress();
                final ICancel ct = cancellationToken != null ? cancellationToken : new NullCancel();
                try {
                    return builder.build(input, pr, ct);
                } catch(InterruptedException e) {
                }
                return null;
            }
        });
    }

    @Override public ITask<?> clean(final CleanInput input, final @Nullable IProgress progressReporter,
        final @Nullable ICancel cancellationToken) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                final IProgress pr = progressReporter != null ? progressReporter : new NullProgress();
                final ICancel ct = cancellationToken != null ? cancellationToken : new NullCancel();
                try {
                    builder.clean(input, pr, ct);
                } catch(InterruptedException e) {
                }
                return null;
            }
        });
    }


    @Override public ITask<?> updateDialects(final FileObject location, final Iterable<ResourceChange> changes) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                dialectProcessor.update(location, changes);
                return null;
            }
        });
    }


    @Override public ITask<?> languageChange(final LanguageComponentChange change) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                languageChangeProcessor.processComponentChange(change);
                return null;
            }
        });
    }

    @Override public ITask<?> languageChange(final LanguageImplChange change) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                languageChangeProcessor.processImplChange(change);
                return null;
            }
        });
    }
}
