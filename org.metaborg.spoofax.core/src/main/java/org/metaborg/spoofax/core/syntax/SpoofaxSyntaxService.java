package org.metaborg.spoofax.core.syntax;

import java.util.Map;
import java.util.Set;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.IParser;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.core.syntax.SyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class SpoofaxSyntaxService extends SyntaxService<ISpoofaxInputUnit, ISpoofaxParseUnit>
    implements ISpoofaxSyntaxService {
    @Inject public SpoofaxSyntaxService(Map<String, IParser<ISpoofaxInputUnit, ISpoofaxParseUnit>> parsers) {
        super(parsers);
    }


    @Override public Iterable<String> singleLineCommentPrefixes(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<String> prefixes = Sets.newLinkedHashSet();
        for(SyntaxFacet facet : facets) {
            Iterables.addAll(prefixes, facet.singleLineCommentPrefixes);
        }
        return prefixes;
    }

    @Override public Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<MultiLineCommentCharacters> chars = Sets.newLinkedHashSet();
        for(SyntaxFacet facet : facets) {
            Iterables.addAll(chars, facet.multiLineCommentCharacters);
        }
        return chars;
    }

    @Override public Iterable<FenceCharacters> fenceCharacters(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<FenceCharacters> fences = Sets.newLinkedHashSet();
        for(SyntaxFacet facet : facets) {
            Iterables.addAll(fences, facet.fenceCharacters);
        }
        return fences;
    }
}
