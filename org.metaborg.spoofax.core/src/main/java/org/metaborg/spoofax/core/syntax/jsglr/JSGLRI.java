package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
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
    private FileObject file;
    private InputStream is;


    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguage language, FileObject file) {
        this(config, termFactory, language);
        this.file = file;
    }

    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguage language, InputStream is) {
        this(config, termFactory, language);
        this.is = is;
    }

    private JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguage language) {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;

        final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(termFactory));
        this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().parseTable());
        this.errorHandler = new JSGLRParseErrorHandler(this, termFactory);
        assert file != null;
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
        assert file != null || is != null;
        String fileName;
        String input;
        if(file != null) {
            fileName = file.getName().getPath();
            input = IOUtils.toString(file.getContent().getInputStream());
        } else {
            fileName = "From input stream";
            final Scanner scanner = new Scanner(is);
            scanner.useDelimiter("\\A");
            input = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        }

        assert input != null;

        IStrategoTerm ast = null;

        errorHandler.reset();
        currentTokenizer = new NullTokenizer(input, fileName);
        try {
            ast = actuallyParse(input, fileName);
            if(file != null) {
                SourceAttachment.putSource(ast, file, config);
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
        return new ParseResult<IStrategoTerm>(ast, file, errorHandler.getCollectedMessages(), -1, language);
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
        return file;
    }

    protected SGLR getParser() {
        return parser;
    }
}
