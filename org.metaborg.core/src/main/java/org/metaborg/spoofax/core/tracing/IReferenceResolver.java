package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;

/**
 * Interface for reference resolution, resolving use sites to their definition sites.
 * 
 * @param <P>
 *            Type of parsed fragments
 * @param <A>
 *            Type of analyzed fragments
 */
public interface IReferenceResolver<P, A> {
    /**
     * Attempt to resolve use site at {@code offset} in the source text, using given analysis result for resolving and
     * tracing.
     * 
     * @param offset
     *            Offset in the source text to perform reference resolution for.
     * @param result
     *            Analysis result to use for resolving and tracing.
     * 
     * @return Resolution if reference resolution was successful, or null if no resolution could be made.
     * @throws SpoofaxException
     *             When reference resolution fails unexpectedly.
     */
    public abstract @Nullable Resolution resolve(int offset, AnalysisFileResult<P, A> result) throws SpoofaxException;
}
