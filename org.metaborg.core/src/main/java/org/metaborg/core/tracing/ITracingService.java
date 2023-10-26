package org.metaborg.core.tracing;

import jakarta.annotation.Nullable;

import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

/**
 * Interface for tracing between parsed, analyzed, and transformed fragments and results, and their region in source
 * files.
 * 
 * @param <P>
 *            Type of parse units
 * @param <A>
 *            Type of analyze units.
 * @param <T>
 *            Type of transform units.
 * @param <F>
 *            Type of fragments.
 */
public interface ITracingService<P extends IParseUnit, A extends IAnalyzeUnit, T extends ITransformUnit<?>, F> {
    /**
     * Retrieves the source location of given fragment.
     * 
     * @param fragment
     *            Fragment to get source location for.
     * @return Source location, or null if it could not be determined.
     */
    @Nullable ISourceLocation location(F fragment);


    /**
     * Retrieves the originating fragment of given fragment.
     * 
     * @param fragment
     *            Fragment to get origin for.
     * @return Originating fragment, or null if it could not be determined.
     */
    @Nullable F origin(F fragment);


    /**
     * Finds a fragment and its ancestors that contain given region, in given parse result.
     * 
     * @param result
     *            Parsed result to get fragments from.
     * @param region
     *            Region inside the result to get fragments for.
     * @return Fragment and its ancestors that contain given region. The returned iterable iterates from deepest (leaf)
     *         fragment to outermost (root) fragment. An empty iterable is returned when no fragments could be found.
     */
    Iterable<F> fragments(P result, ISourceRegion region);

    /**
     * Finds a fragment and its ancestors that contain given region, in given analysis file result.
     * 
     * @param result
     *            Analyzed result to get fragments from.
     * @param region
     *            Region inside the result to get fragments for.
     * @return Fragment and its ancestors that contain given region. The returned iterable iterates from deepest (leaf)
     *         fragment to outermost (root) fragment. An empty iterable is returned when no fragments could be found.
     */
    Iterable<F> fragments(A result, ISourceRegion region);

    /**
     * Finds a fragment and its ancestors that contain given region, in given transform result.
     * 
     * @param result
     *            Transformed result to get fragments from.
     * @param region
     *            Region inside the to get fragments for.
     * @return Fragment and its ancestors that contain given region. The returned iterable iterates from deepest (leaf)
     *         fragment to outermost (root) fragment. An empty iterable is returned when no fragments could be found.
     */
    Iterable<F> fragments(T result, ISourceRegion region);

    /**
     * Finds all fragments contained within the given region.
     * 
     * This only returns the outermost fragments that are contained in the region. Their children are trivially also
     * contained in the region and will not be added separately to the returned result.
     * 
     * @param result
     *            Parsed result to get fragments from.
     * @param region
     *            Region inside the result to get fragments for.
     * @return Fragments contained within the given region. An empty iterable is returned when no fragments could be
     *         found.
     */
    Iterable<F> fragmentsWithin(P result, ISourceRegion region);

    /**
     * Finds all fragments contained within the given region.
     * 
     * This only returns the outermost fragments that are contained in the region. Their children are trivially also
     * contained in the region and will not be added separately to the returned result.
     * 
     * @param result
     *            Analyzed result to get fragments from.
     * @param region
     *            Region inside the result to get fragments for.
     * @return Fragments contained within the given region. An empty iterable is returned when no fragments could be
     *         found.
     */
    Iterable<F> fragmentsWithin(A result, ISourceRegion region);

    /**
     * Finds all fragments contained within the given region.
     * 
     * This only returns the outermost fragments that are contained in the region. Their children are trivially also
     * contained in the region and will not be added separately to the returned result.
     * 
     * @param result
     *            Transformed result to get fragments from.
     * @param region
     *            Region inside the result to get fragments for.
     * @return Fragments contained within the given region. An empty iterable is returned when no fragments could be
     *         found.
     */
    Iterable<F> fragmentsWithin(T result, ISourceRegion region);
}
