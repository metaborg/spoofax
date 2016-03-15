package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.BuildInput;
import org.metaborg.core.build.CleanInput;
import org.metaborg.core.build.IBuildOutput;
import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

/**
 * Interface for creating processing tasks. Used internally, clients should use a {@link IProcessorRunner}.
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
public interface IProcessor<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>> {
    /**
     * @see IProcessorRunner#build(BuildInput, IProgressReporter, ICancellationToken)
     */
    ITask<IBuildOutput<P, A, AU, T>> build(BuildInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken);

    /**
     * @see IProcessorRunner#clean(CleanInput, IProgressReporter, ICancellationToken)
     */
    ITask<?> clean(CleanInput input, @Nullable IProgressReporter progressReporter,
        @Nullable ICancellationToken cancellationToken);


    /**
     * @see IProcessorRunner#updateDialects(FileObject, Iterable)
     */
    ITask<?> updateDialects(FileObject location, Iterable<ResourceChange> changes);


    /**
     * Creates a task that processes given language component change.
     * 
     * @param change
     *            Language implementation change to process.
     * @return Task that processes given language change.
     */
    ITask<?> languageChange(LanguageComponentChange change);

    /**
     * Creates a task that processes given language implementation change.
     * 
     * @param change
     *            Language implementation change to process.
     * @return Task that processes given language change.
     */
    ITask<?> languageChange(LanguageImplChange change);
}
