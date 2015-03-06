package org.metaborg.spoofax.core.style;

import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

public class StylerService implements IStylerService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(StylerService.class);


    @Override public Iterable<IRegionStyle<IStrategoTerm>> styleParsed(ILanguage language,
        Iterable<IRegionCategory<IStrategoTerm>> categorization) {
        final StylerFacet facet = language.facet(StylerFacet.class);
        final List<IRegionStyle<IStrategoTerm>> regionStyles = Lists.newLinkedList();
        for(IRegionCategory<IStrategoTerm> regionCategory : categorization) {
            final IRegionStyle<IStrategoTerm> regionStyle = style(facet, regionCategory);
            if(regionStyle != null) {
                regionStyles.add(regionStyle);
            }
        }

        return regionStyles;
    }

    @Override public Iterable<IRegionStyle<IStrategoTerm>> styleAnalyzed(ILanguage language,
        Iterable<IRegionCategory<IStrategoTerm>> categorization) {
        throw new UnsupportedOperationException();
    }

    private @Nullable IRegionStyle<IStrategoTerm>
        style(StylerFacet facet, IRegionCategory<IStrategoTerm> regionCategory) {
        final ICategory category = regionCategory.category();
        // GTODO: instanceof checks are nasty, but required since we do not have separate specifications for categories
        // and styles, they are intertwined.
        final IStyle style;
        if(category instanceof SortConsCategory) {
            final SortConsCategory cat = (SortConsCategory) category;
            style = facet.sortConsStyle(cat.sort, cat.cons);
        } else if(category instanceof ConsCategory) {
            final ConsCategory cat = (ConsCategory) category;
            style = facet.consStyle(cat.cons);
        } else if(category instanceof SortCategory) {
            final SortCategory cat = (SortCategory) category;
            style = facet.sortStyle(cat.sort);
        } else if(category instanceof TokenCategory) {
            final TokenCategory cat = (TokenCategory) category;
            style = facet.tokenStyle(cat.token);
        } else {
            style = null;
        }

        if(style == null) {
            logger.warn("Cannot determine style for category " + category.name());
            return null;
        }

        return new RegionStyle<IStrategoTerm>(regionCategory.fragment(), regionCategory.region(), style);
    }
}
