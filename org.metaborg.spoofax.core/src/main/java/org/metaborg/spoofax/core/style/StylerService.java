package org.metaborg.spoofax.core.style;

import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.style.ICategory;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

public class StylerService implements ISpoofaxStylerService {
    private static final ILogger logger = LoggerUtils.logger(StylerService.class);


    @Override public Iterable<IRegionStyle<IStrategoTerm>> styleParsed(ILanguageImpl language,
        Iterable<IRegionCategory<IStrategoTerm>> categorization) {
        final StylerFacet facet = language.facet(StylerFacet.class);
        if(facet == null) {
            logger.error("Cannot style input of {}, it does not have a styler facet", language);
            // GTODO: throw exception instead
            return Iterables2.empty();
        }

        final List<IRegionStyle<IStrategoTerm>> regionStyles = Lists.newLinkedList();
        for(IRegionCategory<IStrategoTerm> regionCategory : categorization) {
            final IRegionStyle<IStrategoTerm> regionStyle = style(facet, regionCategory);
            if(regionStyle != null) {
                regionStyles.add(regionStyle);
            }
        }

        return regionStyles;
    }

    @Override public Iterable<IRegionStyle<IStrategoTerm>> styleAnalyzed(ILanguageImpl language,
        Iterable<IRegionCategory<IStrategoTerm>> categorization) {
        throw new UnsupportedOperationException();
    }

    private @Nullable IRegionStyle<IStrategoTerm> style(StylerFacet facet,
        IRegionCategory<IStrategoTerm> regionCategory) {
        if(regionCategory.region().length() == 0) {
            // Skip empty regions for styling.
            return null;
        }
        final ICategory category = regionCategory.category();
        // HACK: instanceof checks are nasty, but required since we do not have separate specifications for categories
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

        return new RegionStyle<>(regionCategory.region(), style, regionCategory.fragment());
    }
}
