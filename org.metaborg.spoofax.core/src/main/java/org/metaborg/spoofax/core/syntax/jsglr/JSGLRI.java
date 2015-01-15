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
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ITreeFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

public class JSGLRI implements IFileParser<IStrategoTerm> {
    private final IParserConfig config;
    private final ITermFactory termFactory;
    private final ILanguage language;
    private final JSGLRParseErrorHandler errorHandler;

    private SGLR parser;
    private Disambiguator disambiguator;
    private int cursorLocation = Integer.MAX_VALUE;
    private boolean useRecovery = false;
    private boolean implodeEnabled = true;
    private ITokenizer currentTokenizer;
    private FileObject resource;
    private String input;


    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguage language, FileObject resource,
        String input) {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.resource = resource;
        this.input = input;

        final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(termFactory));
        this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().parseTable());
        this.errorHandler = new JSGLRParseErrorHandler(this, termFactory);
        assert resource != null;
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

    /**
     * Resets the state of this parser, reinitializing the SGLR instance
     */
    private void resetState() {
        parser.setTimeout(config.getTimeout());
        if(disambiguator != null)
            parser.setDisambiguator(disambiguator);
        else
            disambiguator = parser.getDisambiguator();
        setUseRecovery(useRecovery);
        if(!implodeEnabled) {
            parser.setTreeBuilder(new Asfix2TreeBuilder(termFactory));
        } else {
            assert parser.getTreeBuilder() instanceof TreeBuilder;
            @SuppressWarnings("unchecked") ITreeFactory<IStrategoTerm> treeFactory =
                ((TreeBuilder) parser.getTreeBuilder()).getFactory();
            assert ((TermTreeFactory) treeFactory).getOriginalTermFactory() instanceof ParentTermFactory;
        }
    }

    @Override public ParseResult<IStrategoTerm> parse() throws IOException {
        final String fileName = resource.getName().getPath();

        IStrategoTerm ast = null;

        errorHandler.reset();
        currentTokenizer = new NullTokenizer(input, fileName);
        try {
            ast = actuallyParse(input, fileName);
            if(resource != null) {
                SourceAttachment.putSource(ast, resource, config);
            }
        } catch(Exception e) {
            errorHandler.setRecoveryFailed(true);
            errorHandler.gatherException(currentTokenizer, e);
        }

        if(ast != null) {
            currentTokenizer = ImploderAttachment.getTokenizer(ast);
            errorHandler.setRecoveryFailed(false);
            errorHandler.gatherNonFatalErrors(ast);
        }

        // GTODO: measure parse time
        // GTODO: file can be null, need to handle that!
        return new ParseResult<IStrategoTerm>(ast, resource, errorHandler.getCollectedMessages(), -1,
            language);
    }

    public IStrategoTerm actuallyParse(String input, String filename) throws SGLRException,
        InterruptedException {
        IStrategoTerm result;
        try {
            result =
                (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol(), true, cursorLocation);
        } catch(FilterException fex) {
            if(fex.getCause() == null && parser.getDisambiguator().getFilterPriorities()) {
                disambiguator.setFilterPriorities(false);
                try {
                    result = (IStrategoTerm) parser.parse(input, filename, config.getStartSymbol());
                } finally {
                    disambiguator.setFilterPriorities(true);
                }
            } else {
                throw fex;
            }
        }
        return result;
    }

    @Override public IParserConfig getConfig() {
        return config;
    }

    public ILanguage getLanguage() {
        return language;
    }

    @Override public FileObject getFile() {
        return resource;
    }

    protected SGLR getParser() {
        return parser;
    }
}
