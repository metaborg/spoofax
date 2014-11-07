package org.metaborg.spoofax.core.parser.jsglr;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.parser.IParseService;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.spoofax.core.service.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.io.ParseTableManager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class JSGLRParseService implements IParseService<IStrategoTerm> {
    private final ITermFactoryService termFactoryService;
    private final ParseTableManager parseTableManager;

    private final Map<ILanguage, IParserConfig> parserConfigs = Maps.newHashMap();


    @Inject public JSGLRParseService(ITermFactoryService termFactoryService) {
        this.termFactoryService = termFactoryService;

        final ITermFactory termFactory =
            this.termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        this.parseTableManager = new ParseTableManager(termFactory);
    }


    @Override public ParseResult<IStrategoTerm> parse(FileObject file, ILanguage language) throws IOException {
        final IParserConfig config = getParserConfig(language);
        final JSGLRI parser = new JSGLRI(config, termFactoryService.get(language), file);
        return parser.parse();
    }


    public IParserConfig getParserConfig(ILanguage lang) {
        IParserConfig config = parserConfigs.get(lang);
        if(config == null) {
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);
            final IParseTableProvider provider =
                new FileParseTableProvider(facet.parseTable(), parseTableManager);
            config = new ParserConfig(Iterables.get(facet.startSymbols(), 0), provider, 5000);
            parserConfigs.put(lang, config);
        }
        return config;
    }
}
