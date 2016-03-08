package org.metaborg.core.style;

import javax.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

/**
 * Interface for styles that range over a region in source text. Created by the {@link IStylerService} from a
 * categorization of a parse or analysis result.
 * 
 * @param <T>
 *            Type of fragment from the categorized parse or analysis result.
 */
public interface IRegionStyle<T> {
    /**
     * @return Region in source text over which the style spans.
     */
    ISourceRegion region();

    /**
     * @return Style assigned to the region.
     */
    IStyle style();

    /**
     * @return Fragment associated with the region, or null if there is no such association.
     */
    @Nullable T fragment();
}
