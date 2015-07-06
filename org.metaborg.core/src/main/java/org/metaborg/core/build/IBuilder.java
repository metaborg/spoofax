package org.metaborg.core.build;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.processing.IProgressReporter;

/**
 * Incrementally parses, analyses, and compiles source files.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public interface IBuilder<P, A, T> {
    /**
     * Parses, analyses, and compiles changed resources.
     * 
     * @param input
     *            Build input.
     * @return Result of building.
     * @throws MetaborgRuntimeException
     *             When {@code input.throwOnErrors} is set to true and errors occur.
     */
    public abstract IBuildOutput<P, A, T> build(BuildInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken);

    /**
     * Cleans derived resources and contexts from given location.
     * 
     * @param input
     *            Clean input.
     */
    public abstract void clean(CleanInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken);
}