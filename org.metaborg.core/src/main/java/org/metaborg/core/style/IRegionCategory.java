package org.metaborg.core.style;

import javax.annotation.Nullable;

import org.metaborg.core.source.ISourceRegion;

/**
 * Interface for categories that range over a region in source text. Created by the {@link ICategorizerService} from a
 * parse or analysis result.
 * 
 * @param <T>
 *            Type of fragment from the parse or analysis result.
 */
public interface IRegionCategory<T> {
    /**
     * @return Region in source text over which the category spans.
     */
    public abstract ISourceRegion region();

    /**
     * @return Category assigned to the region.
     */
    public abstract ICategory category();

    /**
     * @return Fragment associated with the region, or null if there is no such association.
     */
    public abstract @Nullable T fragment();
}
