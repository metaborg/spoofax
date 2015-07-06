package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;

public interface IProcessorRunner<P, A, T> {
    public abstract ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter);

    public abstract ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter);
}
