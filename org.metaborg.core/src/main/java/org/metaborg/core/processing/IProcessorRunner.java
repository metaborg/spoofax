package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;

/**
 * Interface for language processing. Handles building and cleaning on-demand, and language change events automatically.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public interface IProcessorRunner<P, A, T> {
    /**
     * Creates a task that builds with given build input.
     * 
     * @param input
     *            Build input to use.
     * @param progressReporter
     *            Progress reporter for the build, or null to use a processor-specific implementation for progress
     *            reporting.
     * @return Task that builds with given input, and has the build output as result. Schedule the task and wait for it
     *         to complete to get the build output.
     */
    public abstract ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter);

    /**
     * Creates a task that cleans with given clean input.
     * 
     * @param input
     *            Clean input to use.
     * @param progressReporter
     *            Progress reporter for the build, or null to use a processor-specific implementation for progress
     *            reporting.
     * @return Task that cleans with given input.
     */
    public abstract ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter);
}
