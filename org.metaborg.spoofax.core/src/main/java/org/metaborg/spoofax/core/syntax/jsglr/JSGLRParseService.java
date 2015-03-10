package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.io.ParseTableManager;
import org.spoofax.terms.util.NotImplementedException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class JSGLRParseService implements ISyntaxService<IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRParseService.class);
    
    private final ITermFactoryService termFactoryService;
    private final ParseTableManager parseTableManager;

    private final Map<ILanguage, IParserConfig> parserConfigs = Maps.newHashMap();


    @Inject public JSGLRParseService(ITermFactoryService termFactoryService) {
        this.termFactoryService = termFactoryService;

        final ITermFactory termFactory =
            this.termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        this.parseTableManager = new ParseTableManager(termFactory);
    }


    @Override public ParseResult<IStrategoTerm> parse(String text, FileObject resource, ILanguage language)
        throws ParseException {
        final IParserConfig config = getParserConfig(language);
        try {
            logger.trace("Parsing {}", resource);
            final JSGLRI parser = new JSGLRI(config, termFactoryService.get(language), language, resource, text);
            return parser.parse();
        } catch(IOException e) {
            throw new ParseException(resource, language, e);
        }
    }

    @Override public String unparse(IStrategoTerm parsed, ILanguage language) {
        throw new NotImplementedException();
    }

    @Override public ISourceRegion region(IStrategoTerm parsed) {
        final IToken left = ImploderAttachment.getLeftToken(parsed);
        final IToken right = ImploderAttachment.getRightToken(parsed);
        if(left == null || right == null)
            return null;
        return JSGLRSourceRegionFactory.fromTokens(left, right);
    }

    public IParserConfig getParserConfig(ILanguage lang) {
        IParserConfig config = parserConfigs.get(lang);
        if(config == null) {
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);
            final IParseTableProvider provider = new FileParseTableProvider(facet.parseTable(), parseTableManager);
            config = new ParserConfig(Iterables.get(facet.startSymbols(), 0), provider, 5000);
            parserConfigs.put(lang, config);
        }
        return config;
    }
}
