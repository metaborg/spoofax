package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.IParserConfiguration;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Asfix2TreeBuilder;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.FilterException;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.StartSymbolException;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

public class JSGLRI {
    private final IParserConfig config;
    private final ITermFactory termFactory;
    private final ILanguage language;
    private final ILanguage dialect;
    private final FileObject resource;
    private final String input;

    private final SGLR parser;


    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguage language, ILanguage dialect,
        FileObject resource, String input) throws IOException {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.dialect = dialect;
        this.resource = resource;
        this.input = input;

        final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(termFactory));
        this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().parseTable());
    }


    public ParseResult<IStrategoTerm> parse(@Nullable IParserConfiguration parserConfig) throws IOException {
        JSGLRParserConfiguration configuration = (JSGLRParserConfiguration) parserConfig;
        if(configuration == null) {
            configuration = new JSGLRParserConfiguration();
        }

        final String fileName = resource.getName().getPath();

        final JSGLRParseErrorHandler errorHandler =
            new JSGLRParseErrorHandler(this, termFactory, resource, config.getParseTableProvider().parseTable()
                .hasRecovers());

        SGLRParseResult result;
        try {
            result = actuallyParse(input, fileName, configuration);
        } catch(SGLRException | InterruptedException e) {
            result = null;
            errorHandler.setRecoveryFailed(configuration.recovery);
            errorHandler.processFatalException(new NullTokenizer(input, fileName), e);
        }

        final IStrategoTerm ast;
        if(result != null) {
            ast = (IStrategoTerm) result.output;
            if(ast != null) {
                errorHandler.setRecoveryFailed(false);
                errorHandler.gatherNonFatalErrors(ast);
                if(resource != null) {
                    SourceAttachment.putSource(ast, resource, config);
                }
            }
        } else {
            ast = null;
        }

        // GTODO: measure parse time
        return new ParseResult<IStrategoTerm>(input, ast, resource, errorHandler.messages(), -1, language, dialect,
            result);
    }

    public SGLRParseResult actuallyParse(String text, String filename, @Nullable JSGLRParserConfiguration parserConfig)
        throws SGLRException, InterruptedException {
        if(!parserConfig.implode) {
            // GTODO: copied from existing code. Is this correct? Seems like this should be the tree builder when
            // implode is set to true. Also, there is no else branch.
            parser.setTreeBuilder(new Asfix2TreeBuilder(termFactory));
        }
        parser.setUseStructureRecovery(parserConfig.recovery);
        parser.setCompletionParse(parserConfig.completion, parserConfig.cursorPosition);
        parser.setTimeout(parserConfig.timeout);

        final Disambiguator disambiguator = parser.getDisambiguator();

        if(dialect != null) {
            disambiguator.setHeuristicFilters(true);
        } else {
            disambiguator.setHeuristicFilters(false);
        }

        try {
            return parser.parse(text, filename, config.getStartSymbol());
        } catch(FilterException e) {
            if(e.getCause() == null && disambiguator.getFilterPriorities()) {
                disambiguator.setFilterPriorities(false);
                try {
                    return parser.parse(text, filename, config.getStartSymbol());
                } finally {
                    disambiguator.setFilterPriorities(true);
                }
            }
            throw e;
        } catch(StartSymbolException e) {
            return parser.parse(text, filename, null);
        }
    }

    public IParserConfig getConfig() {
        return config;
    }

    public ILanguage getLanguage() {
        return language;
    }

    public ILanguage getDialect() {
        return dialect;
    }

    public FileObject getResource() {
        return resource;
    }

    public String getInput() {
        return input;
    }

    protected SGLR getParser() {
        return parser;
    }
}
