package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

import rx.functions.Func0;

import com.google.inject.Inject;

/**
 * Processor implementation that uses {@link BlockingTask} as task implementation. Tasks execute and block when
 * scheduled.
 */
public class BlockingProcessor<P, A, T> implements IProcessor<P, A, T> {
    private final IDialectProcessor dialectProcessor;
    private final IBuilder<P, A, T> builder;
    private final ILanguageChangeProcessor languageChangeProcessor;


    @Inject public BlockingProcessor(IDialectProcessor dialectProcessor, IBuilder<P, A, T> builder,
        ILanguageChangeProcessor languageChangeProcessor) {
        this.dialectProcessor = dialectProcessor;
        this.builder = builder;
        this.languageChangeProcessor = languageChangeProcessor;
    }


    @Override public ITask<IBuildOutput<P, A, T>> build(final BuildInput input,
        final @Nullable IProgressReporter progressReporter) {
        return new BlockingTask<>(new Func0<IBuildOutput<P, A, T>>() {
            @Override public IBuildOutput<P, A, T> call() {
                return builder.build(input, progressReporter != null ? progressReporter : new NullProgressReporter(),
                    new CancellationToken());
            }
        });
    }

    @Override public ITask<?> clean(final CleanInput input, final @Nullable IProgressReporter progressReporter) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                builder.clean(input, progressReporter != null ? progressReporter : new NullProgressReporter(),
                    new CancellationToken());
                return null;
            }
        });
    }


    @Override public ITask<?> updateDialects(final IProject project, final Iterable<ResourceChange> changes) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                dialectProcessor.update(project, changes);
                return null;
            }
        });
    }


    @Override public ITask<?> languageChange(final LanguageChange change) {
        return new BlockingTask<>(new Func0<Object>() {
            @Override public Object call() {
                languageChangeProcessor.process(change, null);
                return null;
            }
        });
    }
}
