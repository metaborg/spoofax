package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.*;

import jakarta.annotation.Nullable;
import jsglr.shared.ImploderAttachment;
import jsglr.shared.NullTokenizer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.messages.MessageUtils;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.collection.SetMultimap;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.*;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.spoofax.terms.attachments.ParentTermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_sglr.implode_asfix_0_0;

public class JSGLR1I extends JSGLRI<ParseTable> {

    private final SGLR parser;
    private final Context context;

    public JSGLR1I(IParserConfig config, ITermFactory termFactory, Context context, ILanguageImpl language,
        ILanguageImpl dialect) throws IOException {
        super(config, termFactory, language, dialect);

        final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(termFactory));
        this.parseTable = getParseTable(config.getParseTableProvider());
        this.parser = new SGLR(new TreeBuilder(factory), parseTable);
        this.context = context;
    }

    @Override public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig, @Nullable FileObject resource,
        String input) {
        if(parserConfig == null) {
            parserConfig = new JSGLRParserConfiguration();
        }

        final String fileName = resource != null ? resource.getName().getURI() : null;

        final JSGLRParseErrorHandler errorHandler =
            new JSGLRParseErrorHandler(this, resource, parseTable.hasRecovers());

        final Timer timer = new Timer(true);
        SGLRParseResult result;
        try {
            // should throw a fatal, or return a non-null result
            result = actuallyParse(input, fileName, parserConfig);
            assert result != null;
        } catch(SGLRException | InterruptedException e) {
            result = null;
            errorHandler.setRecoveryFailed(parserConfig.recovery);
            errorHandler.processFatalException(new NullTokenizer(input, fileName), e);
        }
        final long duration = timer.stop();

        final IStrategoTerm ast;
        if(result != null) {
            // No fatals occurred, so either parsing succeeded or recovery succeeded
            ast = (IStrategoTerm) result.output;
            if(ast == null) {
                // this should only happen if we passed a NullTreeBuilder and parsing succeeded
                // so we have nothing to do
                assert parser.getTreeBuilder() instanceof NullTreeBuilder;
            } else {
                // in case recovery was required, collect the recoverable errors
                errorHandler.setRecoveryFailed(false);
                errorHandler.gatherNonFatalErrors(ast);
                if(resource != null) {
                    SourceAttachment.putSource(ast, resource);
                }
            }
        } else {
            ast = null;
        }

        final boolean hasAst = ast != null;
        final List<IMessage> messages = new ArrayList<>();
        for(IMessage message : errorHandler.messages()) {
            messages.add(message);
        }

        // add non-assoc warnings to messages
        messages.addAll(addNonAssocErrorMessages(ast, resource));

        if(config.getImploderSetting() == ImploderImplementation.stratego) {
            for(BadTokenException badTokenException : parser.getCollectedErrors()) {
                final String message = badTokenException.getMessage();
                messages.add(MessageFactory.newParseErrorAtTop(resource, message, null));
            }
        }
        final boolean hasErrors = MessageUtils.containsSeverity(messages, MessageSeverity.ERROR);
        final boolean isAmbiguous = hasAst && isAmbiguous(ast);
        return new ParseContrib(hasAst, hasAst && !hasErrors, isAmbiguous, ast, messages, duration);
    }

    /**
     * Determines whether the AST has any ambiguities by traversing
     * the AST until it finds an amb() node.
     *
     * @param ast the AST to check
     * @return {@code true} when the AST has at least one ambiguity;
     * otherwise, {@code false}
     */
    private boolean isAmbiguous(IStrategoTerm ast) {
        LinkedList<IStrategoTerm> worklist = new LinkedList<>();
        worklist.add(ast);
        while (!worklist.isEmpty()) {
            IStrategoTerm term = worklist.pop();
            if (TermUtils.isAppl(term, "amb")) return true;
            worklist.addAll(term.getSubterms());
        }
        return false;
    }

    public SGLRParseResult actuallyParse(String text, @Nullable String filename,
        @Nullable JSGLRParserConfiguration parserConfig) throws SGLRException, InterruptedException {
        if(!parserConfig.implode || config.getImploderSetting() == ImploderImplementation.stratego) {
            // GTODO: copied from existing code. Is this correct? Seems like this should be the tree builder when
            // implode is set to true. Also, there is no else branch.
            parser.setTreeBuilder(new Asfix2TreeBuilder(termFactory));
        }
        parser.setUseStructureRecovery(parserConfig.recovery);
        if(parserConfig.cursorPosition == Integer.MAX_VALUE) {
            parser.setCompletionParse(false, Integer.MAX_VALUE);
        } else {
            parser.setCompletionParse(parserConfig.completion, parserConfig.cursorPosition);
        }
        parser.setTimeout(parserConfig.timeout);

        final Disambiguator disambiguator = parser.getDisambiguator();

        if(dialect != null) {
            disambiguator.setHeuristicFilters(true);
        } else {
            disambiguator.setHeuristicFilters(false);
        }

        SGLRParseResult parseResult =
            parseAndRecover(text, filename, disambiguator, getOrDefaultStartSymbol(parserConfig));
        if(config.getImploderSetting() == ImploderImplementation.stratego) {
            org.strategoxt.stratego_sglr.Main.init(context);
            final implode_asfix_0_0 imploder = implode_asfix_0_0.instance;
            final IStrategoTerm syntaxTree = (IStrategoTerm) parseResult.output;
            return new SGLRParseResult(imploder.invoke(context, syntaxTree));
        } else {
            return parseResult;
        }
    }

    public SGLRParseResult parseAndRecover(String text, String filename, final Disambiguator disambiguator,
        String startSymbol) throws BadTokenException, TokenExpectedException, ParseException, SGLRException,
        InterruptedException, FilterException, StartSymbolException {
        try {
            return parser.parse(text, filename, startSymbol);
        } catch(FilterException e) {
            if((e.getCause() == null || e.getCause() instanceof UnsupportedOperationException)
                && disambiguator.getFilterPriorities()) {
                disambiguator.setFilterPriorities(false);
                disambiguator.setFilterAssociativity(false);
                try {
                    return parser.parse(text, filename, startSymbol);
                } finally {
                    disambiguator.setFilterPriorities(true);
                }
            }
            throw e;
        } catch(StartSymbolException e) {
            if(dialect != null) {
                // Parse with all symbols as start symbol when start symbol cannot be found and a dialect is set,
                // indicating that we're parsing Stratego with concrete syntax extensions. We need to parse with all
                // symbols as start symbol, because the start symbol is unknown.
                return parser.parse(text, filename, null);
            } else {
                throw e;
            }
        }
    }

    @Override public Set<BadTokenException> getCollectedErrors() {
        return parser.getCollectedErrors();
    }

    protected Collection<? extends IMessage> addNonAssocErrorMessages(IStrategoTerm ast,
        @Nullable FileObject resource) {
        List<IMessage> result = new ArrayList<>();

        boolean addedMessage = false;

        // non-associative and non-nested operators should be flagged with warnings
        if(TermUtils.isAppl(ast) && ast.getAllSubterms().length >= 1) {
            SetMultimap<String, String> nonAssocProductions = parseTable.getNonAssocProductions();
            SetMultimap<String, String> nonNestedProductions = parseTable.getNonNestedProductions();

            String sortConsParent =
                ImploderAttachment.getSort(ast) + "." + ((IStrategoAppl) ast).getConstructor().getName();

            IStrategoTerm firstChild = ast.getSubterm(0);
            IStrategoTerm lastChild = ast.getSubterm(ast.getSubtermCount() - 1);

            if(TermUtils.isAppl(firstChild)) {
                IStrategoAppl leftMostChild = (IStrategoAppl) firstChild;
                @Nullable ImploderAttachment leftMostChildAttachment = ImploderAttachment.get(leftMostChild);
                String sortConsChild = ImploderAttachment.getSort(ast) + "." + leftMostChild.getConstructor().getName();
                if(leftMostChildAttachment != null && !leftMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable
                    && nonAssocProductions.containsEntry(sortConsParent, sortConsChild)) {
                    ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(ImploderAttachment.getLeftToken(ast),
                        ImploderAttachment.getRightToken(ast));
                    result.add(MessageFactory.newParseError(resource, region, "Operator is non-associative", null));
                    addedMessage = true;
                }
            }

            if(TermUtils.isAppl(lastChild)) {
                IStrategoAppl rightMostChild = (IStrategoAppl) lastChild;
                @Nullable ImploderAttachment rightMostChildAttachment = ImploderAttachment.get(rightMostChild);
                String sortConsChild =
                    ImploderAttachment.getSort(ast) + "." + rightMostChild.getConstructor().getName();
                if(rightMostChildAttachment != null && !rightMostChildAttachment.isBracket()
                    && parseTable instanceof ParseTable
                    && nonNestedProductions.containsEntry(sortConsParent, sortConsChild)) {
                    ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(ImploderAttachment.getLeftToken(ast),
                        ImploderAttachment.getRightToken(ast));
                    result.add(MessageFactory.newParseError(resource, region, "Operator is non-nested", null));
                    addedMessage = true;
                }
            }
        }

        if(ast != null && !addedMessage) {
            for(IStrategoTerm child : ast.getAllSubterms()) {
                result.addAll(addNonAssocErrorMessages(child, resource));
            }
        }

        return result;
    }

}
