package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.language.LanguageChange;

import rx.functions.Func0;

public class BlockingProcessor<P, A, T> implements IProcessor<P, A, T> {
    private final IBuilder<P, A, T> builder;


    public BlockingProcessor(IBuilder<P, A, T> builder) {
        this.builder = builder;
    }


    @Override public ITask<IBuildOutput<P, A, T>> build(final BuildInput input,
        final @Nullable IProgressReporter progressReporter) {
        return new BlockingTask<IBuildOutput<P, A, T>>(new Func0<IBuildOutput<P, A, T>>() {
            @Override public IBuildOutput<P, A, T> call() {
                return builder.build(input, progressReporter != null ? progressReporter : new NullProgressReporter(),
                    new CancellationToken());
            }
        });
    }

    @Override public ITask<?> clean(final CleanInput input, final @Nullable IProgressReporter progressReporter) {
        return new BlockingTask<Object>(new Func0<Object>() {
            @Override public Object call() {
                builder.clean(input, progressReporter != null ? progressReporter : new NullProgressReporter(),
                    new CancellationToken());
                return null;
            }
        });
    }

    @Override public ITask<?> languageChange(LanguageChange change) {
        // TODO Auto-generated method stub
        return null;
    }
}
