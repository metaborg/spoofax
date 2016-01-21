package org.metaborg.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformResult;

/**
 * Interface for tracing between parsed, analyzed, and transformed fragments and results, and their region in source
 * files.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public interface ITracingService<P, A, T> {
    /**
     * Retrieves the source location of given parsed fragment.
     * 
     * @param fragment
     *            Parsed fragment to get source location for.
     * @return Source location, or null if it could not be determined.
     */
    public abstract @Nullable ISourceLocation fromParsed(P fragment);

    /**
     * Retrieves the source location of given analyzed fragment.
     * 
     * @param fragment
     *            Analyzed fragment to get source location for.
     * @return Source location, or null if it could not be determined.
     */
    public abstract @Nullable ISourceLocation fromAnalyzed(A fragment);

    /**
     * Retrieves the source location of given transformed fragment.
     * 
     * @param fragment
     *            Transformed fragment to get source location for.
     * @return Source location, or null if it could not be determined.
     */
    public abstract @Nullable ISourceLocation fromTransformed(T fragment);


    /**
     * Retrieves the originating parsed fragment of given analyzed fragment.
     * 
     * @param fragment
     *            Analyzed fragment to get origin for.
     * @return Originating parsed fragment, or null if it could not be determined.
     */
    public abstract @Nullable P originFromAnalyzed(A fragment);

    /**
     * Retrieves the originating parsed fragment of given transformed fragment.
     * 
     * @param fragment
     *            Transformed fragment to get origin for.
     * @return Originating parsed fragment, or null if it could not be determined.
     */
    public abstract @Nullable P originFromTransformed(T fragment);


    /**
     * Finds a parsed fragment and its ancestors that contain given region, in given parse result.
     * 
     * @param result
     *            Parse result to get fragments from.
     * @param region
     *            Region inside the parse result to get fragments for.
     * @return Parsed fragment and its ancestors that contain given region. The returned iterable iterates from deepest
     *         (leaf) fragment to outermost (root) fragment. An empty iterable is returned when no fragments could be
     *         found.
     */
    public abstract Iterable<P> toParsed(ParseResult<P> result, ISourceRegion region);

    /**
     * Finds an analyzed fragment and its ancestors that contain given region, in given analysis file result.
     * 
     * @param result
     *            Analysis file result to get fragments from.
     * @param region
     *            Region inside the analysis file result to get fragments for.
     * @return Analyzed fragment and its ancestors that contain given region. The returned iterable iterates from
     *         deepest (leaf) fragment to outermost (root) fragment. An empty iterable is returned when no fragments
     *         could be found.
     */
    public abstract Iterable<A> toAnalyzed(AnalysisFileResult<P, A> result, ISourceRegion region);

    /**
     * Finds a transformed fragment and its ancestors that contain given region, in given transform result.
     * 
     * @param result
     *            Transform result to get fragments from.
     * @param region
     *            Region inside the transform result to get fragments for.
     * @return Transformed fragment and its ancestors that contain given region. The returned iterable iterates from
     *         deepest (leaf) fragment to outermost (root) fragment. An empty iterable is returned when no fragments
     *         could be found.
     */
    public abstract Iterable<T> toTransformed(TransformResult<?, T> result, ISourceRegion region);
}
