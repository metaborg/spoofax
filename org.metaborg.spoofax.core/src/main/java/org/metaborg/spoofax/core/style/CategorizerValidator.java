package org.metaborg.spoofax.core.style;

import java.util.Collection;

import org.metaborg.spoofax.core.source.ISourceRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class CategorizerValidator {
    private static final Logger logger = LoggerFactory.getLogger(CategorizerValidator.class);

    public static <T> Iterable<IRegionCategory<T>> validate(Iterable<IRegionCategory<T>> categorization) {
        int offset = -1;
        final Collection<IRegionCategory<T>> validated = Lists.newLinkedList();
        for(IRegionCategory<T> regionCategory : categorization) {
            final ISourceRegion region = regionCategory.region();
            if(offset > region.startOffset()) {
                logger.warn("Invalid {}, starting offset is greater than offset in previous regions, "
                    + "region category will be skipped", regionCategory);
            } else if(offset > region.endOffset()) {
                logger.warn("Invalid {}, ending offset is greater than offset in previous regions, "
                    + "region category will be skipped", regionCategory);
            } else if(region.startOffset() > region.endOffset()) {
                logger.warn("Invalid {}, starting offset is greater than ending offset, "
                    + "region category will be skipped", regionCategory);
            } else {
                validated.add(regionCategory);
                offset = regionCategory.region().endOffset();
            }
        }
        return validated;
    }
}
