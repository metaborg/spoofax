package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
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

public class JSGLRI implements IParser<IStrategoTerm> {
    private final IParserConfig config;
    private final ITermFactory termFactory;
    private final ILanguage language;
    private final ILanguage dialect;
    private final FileObject resource;
    private final String input;

    private final SGLR parser;

    private Disambiguator disambiguator;
    private int cursorLocation = Integer.MAX_VALUE;
    private boolean useRecovery = true;
    private boolean implodeEnabled = true;


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

        resetState();
    }


    public void setCursorLocation(int cursorLocation) {
        this.cursorLocation = cursorLocation;
    }

    public void setUseRecovery(boolean useRecovery) {
        this.useRecovery = useRecovery;
        parser.setUseStructureRecovery(useRecovery);
    }

    public void setImplodeEnabled(boolean implode) {
        this.implodeEnabled = implode;
        resetState();
    }

    private void resetState() {
        parser.setTimeout(config.getTimeout());

        if(disambiguator != null) {
            parser.setDisambiguator(disambiguator);
        } else {
            disambiguator = parser.getDisambiguator();
        }

        setUseRecovery(useRecovery);

        if(!implodeEnabled) {
            parser.setTreeBuilder(new Asfix2TreeBuilder(termFactory));
        }
    }

    @Override public ParseResult<IStrategoTerm> parse() throws IOException {
        final String fileName = resource.getName().getPath();

        final JSGLRParseErrorHandler errorHandler =
            new JSGLRParseErrorHandler(this, termFactory, resource, config.getParseTableProvider().parseTable()
                .hasRecovers());

        SGLRParseResult result;
        try {
            result = actuallyParse(input, fileName);
        } catch(SGLRException | InterruptedException e) {
            result = null;
            errorHandler.setRecoveryFailed(useRecovery);
            errorHandler.processFatalException(new NullTokenizer(input, fileName), e);
        }

        final IStrategoTerm ast;
        if(result != null) {
            ast = (IStrategoTerm) result.output;
            errorHandler.setRecoveryFailed(false);
            errorHandler.gatherNonFatalErrors(ast);
            if(resource != null) {
                SourceAttachment.putSource(ast, resource, config);
            }
        } else {
            ast = null;
        }

        // GTODO: measure parse time
        return new ParseResult<IStrategoTerm>(input, ast, resource, errorHandler.messages(), -1, language, dialect,
            result);
    }

    public SGLRParseResult actuallyParse(String text, String filename) throws SGLRException, InterruptedException {
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
