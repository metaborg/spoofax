package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.NotImplementedException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class JSGLRSyntaxService implements ISyntaxService<IStrategoTerm>, ILanguageCache {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRSyntaxService.class);

    private final IDialectService dialectService;
    private final ITermFactoryService termFactoryService;

    private final Map<ILanguageImpl, IParserConfig> parserConfigs = Maps.newHashMap();


    @Inject public JSGLRSyntaxService(IDialectService dialectService, ITermFactoryService termFactoryService) {
        this.dialectService = dialectService;
        this.termFactoryService = termFactoryService;
    }


    @Override public ParseResult<IStrategoTerm> parse(String text, @Nullable FileObject resource, ILanguageImpl language,
        @Nullable IParserConfiguration parserConfig) throws ParseException {
        final IParserConfig config = getParserConfig(language);
        try {
            logger.trace("Parsing {}", resource);
            final ILanguageImpl base = dialectService.getBase(language);
            final JSGLRI parser;
            if(base != null) {
                parser = new JSGLRI(config, termFactoryService.get(language), base, language, resource, text);
            } else {
                parser = new JSGLRI(config, termFactoryService.get(language), language, null, resource, text);
            }
            return parser.parse(parserConfig);
        } catch(IOException e) {
            throw new ParseException(resource, language, e);
        }
    }

    @Override public String unparse(IStrategoTerm parsed, ILanguageImpl language) {
        throw new NotImplementedException();
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


    public IParserConfig getParserConfig(ILanguageImpl lang) {
        IParserConfig config = parserConfigs.get(lang);
        if(config == null) {
            final ITermFactory termFactory =
                termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);
            final IParseTableProvider provider = new FileParseTableProvider(facet.parseTable, termFactory);
            config = new ParserConfig(Iterables.get(facet.startSymbols, 0), provider);
            parserConfigs.put(lang, config);
        }
        return config;
    }


    @Override public ParseResult<IStrategoTerm> emptyParseResult(FileObject resource, ILanguageImpl language,
        @Nullable ILanguageImpl dialect) {
        return new ParseResult<IStrategoTerm>("", termFactoryService.getGeneric().makeTuple(), resource,
            Iterables2.<IMessage>empty(), -1, language, dialect, null);
    }


    @Override public void invalidateCache(ILanguageImpl impl) {
        logger.debug("Removing cached parse table for {}", impl);
        parserConfigs.remove(impl);
    }

    @Override public void invalidateCache(ILanguageComponent component) {

    }
}
