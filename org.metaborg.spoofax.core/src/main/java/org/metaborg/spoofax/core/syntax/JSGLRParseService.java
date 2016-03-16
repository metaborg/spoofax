package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class JSGLRParseService implements ISpoofaxParser, ILanguageCache {
    public static final String name = "jsglr";
    
    private static final ILogger logger = LoggerUtils.logger(JSGLRParseService.class);

    private final ISpoofaxUnitService unitService;
    private final ITermFactoryService termFactoryService;

    private final Map<ILanguageImpl, IParserConfig> parserConfigs = Maps.newHashMap();


    @Inject public JSGLRParseService(ISpoofaxUnitService unitService, ITermFactoryService termFactoryService) {
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
    }


    @Override public ISpoofaxParseUnit parse(ISpoofaxInputUnit input) throws ParseException {
        final FileObject source = input.source();
        final ILanguageImpl langImpl;
        final ILanguageImpl base;
        if(input.dialect() != null) {
            langImpl = input.dialect();
            base = input.langImpl();
        } else {
            langImpl = input.langImpl();
            base = null;
        }
        final String text = input.text();

        // WORKAROUND: The parser can't handle an empty input string.
        if(text == null || text.isEmpty()) {
            final IMessage message = MessageFactory.newParseErrorAtTop(source, "The input is empty", null);
            return unitService.parseUnit(input,
                new ParseContrib(false, false, null, Iterables2.singleton(message), -1));
        }

        final IParserConfig config = getParserConfig(langImpl);
        try {
            logger.trace("Parsing {}", source);
            final JSGLRI parser;
            if(base != null) {
                parser = new JSGLRI(config, termFactoryService.get(langImpl), base, langImpl, source, text);
            } else {
                parser = new JSGLRI(config, termFactoryService.get(langImpl), langImpl, null, source, text);
            }
            final ParseContrib contrib = parser.parse(input.config());
            final ISpoofaxParseUnit unit = unitService.parseUnit(input, contrib);
            return unit;
        } catch(IOException e) {
            throw new ParseException(input, e);
        }
    }

    @Override public Collection<ISpoofaxParseUnit> parseAll(Iterable<ISpoofaxInputUnit> inputs) throws ParseException {
        final Collection<ISpoofaxParseUnit> parseUnits = Lists.newArrayList();
        for(ISpoofaxInputUnit input : inputs) {
            parseUnits.add(parse(input));
        }
        return parseUnits;
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


    @Override public void invalidateCache(ILanguageImpl impl) {
        logger.debug("Removing cached parse table for {}", impl);
        parserConfigs.remove(impl);
    }

    @Override public void invalidateCache(ILanguageComponent component) {

    }
}
