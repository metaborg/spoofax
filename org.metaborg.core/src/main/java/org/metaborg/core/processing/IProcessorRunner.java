package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.resource.ResourceChange;

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
     *            Progress reporter, or null to use a processor-specific implementation for progress reporting.
     * @param cancellationToken
     *            Cancellation token, or null to a use a processor-specific implementation for cancellation.
     * @return Task that builds with given input, and has the build output as result. Schedule the task and wait for it
     *         to complete to get the build output.
     */
    ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter,
                                       @Nullable ICancellationToken cancellationToken);

    /**
     * Creates a task that cleans with given clean input.
     * 
     * @param input
     *            Clean input to use.
     * @param progressReporter
     *            Progress reporter, or null to use a processor-specific implementation for progress reporting.
     * @param cancellationToken
     *            Cancellation token, or null to a use a processor-specific implementation for cancellation.
     * @return Task that cleans with given input.
     */
    ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter,
                   @Nullable ICancellationToken cancellationToken);


    /**
     * Creates a task that updates dialects using given changes.
     * 
     * @param location
     *            Location to process changes at.
     * @param changes
     *            Resource changes to process.
     * @return Task that processes dialect updates.
     */
    ITask<?> updateDialects(FileObject location, Iterable<ResourceChange> changes);
}
