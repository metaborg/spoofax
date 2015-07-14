package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

/**
 * Interface for creating processing tasks. Used internally, clients should use a {@link IProcessorRunner}.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public interface IProcessor<P, A, T> {
    /**
     * @see IProcessorRunner#build(BuildInput, IProgressReporter)
     */
    public abstract ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter);

    /**
     * @see IProcessorRunner#clean(CleanInput, IProgressReporter)
     */
    public abstract ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter);


    /**
     * @see IProcessorRunner#updateDialects(IProject, Iterable)
     */
    public abstract ITask<?> updateDialects(IProject project, Iterable<ResourceChange> changes);


    /**
     * Creates a task that processes given language change.
     * 
     * @param change
     *            Language change to process.
     * @return Task that processes given language change.
     */
    public abstract ITask<?> languageChange(LanguageChange change);
}
