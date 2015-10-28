package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
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
    public abstract ITask<IBuildOutput<P, A, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken);

    /**
     * @see IProcessorRunner#clean(CleanInput, IProgressReporter)
     */
    public abstract ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken);


    /**
     * @see IProcessorRunner#updateDialects(FileObject, Iterable)
     */
    public abstract ITask<?> updateDialects(FileObject location, Iterable<ResourceChange> changes);


    /**
     * Creates a task that processes given language component change.
     * 
     * @param change
     *            Language implementation change to process.
     * @return Task that processes given language change.
     */
    public abstract ITask<?> languageChange(LanguageComponentChange change);

    /**
     * Creates a task that processes given language implementation change.
     * 
     * @param change
     *            Language implementation change to process.
     * @return Task that processes given language change.
     */
    public abstract ITask<?> languageChange(LanguageImplChange change);
}
