package org.metaborg.spoofax.core.syntax;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nullable;

import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseFacet;
import org.metaborg.core.syntax.SyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;


public class SpoofaxSyntaxService extends SyntaxService<ISpoofaxInputUnit, ISpoofaxParseUnit>
    implements ISpoofaxSyntaxService {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxSyntaxService.class);

    @jakarta.inject.Inject public SpoofaxSyntaxService(Map<String, ISpoofaxParser> parsers) {
        super(parsers);
    }


    @Override public Iterable<String> singleLineCommentPrefixes(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<String> prefixes = new LinkedHashSet<>();
        for(SyntaxFacet facet : facets) {
            Iterables2.addAll(prefixes, facet.singleLineCommentPrefixes);
        }
        return prefixes;
    }

    @Override public Iterable<MultiLineCommentCharacters> multiLineCommentCharacters(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<MultiLineCommentCharacters> chars = new LinkedHashSet<>();
        for(SyntaxFacet facet : facets) {
            Iterables2.addAll(chars, facet.multiLineCommentCharacters);
        }
        return chars;
    }

    @Override public Iterable<FenceCharacters> fenceCharacters(ILanguageImpl language) {
        final Iterable<SyntaxFacet> facets = language.facets(SyntaxFacet.class);
        final Set<FenceCharacters> fences = new LinkedHashSet<>();
        for(SyntaxFacet facet : facets) {
            Iterables2.addAll(fences, facet.fenceCharacters);
        }
        return fences;
    }

    @Override public ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel,
        @Nullable JSGLRVersion overrideJSGLRVersion, @Nullable ImploderImplementation overrideImploder) throws ParseException, InterruptedException {
        final ILanguageImpl langImpl = input.langImpl();
        final ISpoofaxParser parser = parser(langImpl);
        if(parser == null) {
            final String message = logger.format("Cannot get a parser for {}", langImpl);
            throw new ParseException(input, message);
        }
        return parser.parse(input, progress, cancel, overrideJSGLRVersion, overrideImploder);
    }


    protected final ISpoofaxParser parser(ILanguageImpl language) {
        final ParseFacet facet = language.facet(ParseFacet.class);
        if(facet == null) {
            return null;
        }
        return (ISpoofaxParser) parsers.get(facet.type);
    }
}
