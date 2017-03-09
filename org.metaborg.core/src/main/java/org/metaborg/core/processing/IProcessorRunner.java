package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

/**
 * Interface for language processing. Handles building and cleaning on-demand, and language change events automatically.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 * @param <T>
 *            Type of transform units.
 */
public interface IProcessorRunner<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>> {
    /**
     * Creates a task that builds with given build input.
     * 
     * @param input
     *            Build input to use.
     * @param progress
     *            Progress reporter, or null to use a processor-specific implementation for progress reporting.
     * @param cancel
     *            Cancellation token, or null to a use a processor-specific implementation for cancellation.
     * @return Task that builds with given input, and has the build output as result. Schedule the task and wait for it
     *         to complete to get the build output.
     */
    ITask<? extends IBuildOutput<P, A, AU, T>> build(BuildInput input, @Nullable IProgress progress,
        @Nullable ICancel cancel);

    /**
     * Creates a task that cleans with given clean input.
     * 
     * @param input
     *            Clean input to use.
     * @param progress
     *            Progress reporter, or null to use a processor-specific implementation for progress reporting.
     * @param cancel
     *            Cancellation token, or null to a use a processor-specific implementation for cancellation.
     * @return Task that cleans with given input.
     */
    ITask<?> clean(CleanInput input, @Nullable IProgress progress, @Nullable ICancel cancel);


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
