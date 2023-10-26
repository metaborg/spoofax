package org.metaborg.core.style;

import jakarta.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

/**
 * Interface for categories that range over a region in source text. Created by the {@link ICategorizerService} from a
 * parse or analysis result.
 * 
 * @param <F>
 *            Type of fragments.
 */
public interface IRegionCategory<F> {
    /**
     * @return Region in source text over which the category spans.
     */
    ISourceRegion region();

    /**
     * @return Category assigned to the region.
     */
    ICategory category();

    /**
     * @return Fragment associated with the region, or null if there is no such association.
     */
    @Nullable F fragment();
}
