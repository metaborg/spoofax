package org.metaborg.spoofax.core.completion;

import static mb.jsglr.shared.IToken.Kind.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import jakarta.annotation.Nullable;
import mb.jsglr.shared.IToken;
import mb.jsglr.shared.ITokenizer;
import mb.jsglr.shared.ImploderAttachment;
import mb.jsglr.shared.ListImploderAttachment;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.completion.Completion;
import org.metaborg.core.completion.CompletionKind;
import org.metaborg.core.completion.ICompletion;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceLocation;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.syntax.SourceAttachment;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.util.TermUtils;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;
import org.strategoxt.HybridInterpreter;

public class JSGLRCompletionService implements ISpoofaxCompletionService {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRCompletionService.class);

    private final ITermFactory termFactory;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon strategoCommon;
    private final IResourceService resourceService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;



    @jakarta.inject.Inject public JSGLRCompletionService(ITermFactory termFactory,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon strategoCommon,
        IResourceService resourceService, ISpoofaxUnitService unitService, ISpoofaxSyntaxService syntaxService) {
        this.termFactory = termFactory;
        this.strategoRuntimeService = strategoRuntimeService;
        this.strategoCommon = strategoCommon;
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
    }

    @Override public Iterable<ICompletion> get(int position, ISpoofaxParseUnit parseInput, boolean nested)
        throws MetaborgException {
        ISpoofaxParseUnit completionParseResult = null;

        if(!nested && !parseInput.success()) {
            final JSGLRParserConfiguration config = new JSGLRParserConfiguration(true, true, true, 3000, position);
            final ISpoofaxInputUnit input = parseInput.input();
            final ISpoofaxInputUnit modifiedInput =
                unitService.inputUnit(input.source(), input.text(), input.langImpl(), input.dialect(), config);
            completionParseResult = syntaxService.parse(modifiedInput);
        }

        Collection<ICompletion> completions = new LinkedList<>();

        // Completion in case of empty input
        String inputText = parseInput.input().text();
        if(inputText.trim().isEmpty()) {
            final ILanguageImpl language = parseInput.input().langImpl();
            final FileObject location = parseInput.source();
            final Iterable<String> startSymbols = language.facet(SyntaxFacet.class).startSymbols;
            completions.addAll(completionEmptyProgram(startSymbols, inputText.length(), language, location));

            return completions;
        }


        if(completionParseResult != null && completionParseResult.ast() == null) {
            return completions;
        }

        Collection<IStrategoTerm> nestedCompletionTerms = getNestedCompletionTermsFromAST(completionParseResult);
        Collection<IStrategoTerm> completionTerms = getCompletionTermsFromAST(completionParseResult);

        boolean blankLineCompletion = isCompletionBlankLine(position, parseInput.input().text());

        if(!completionTerms.isEmpty()) {
            completions.addAll(completionErroneousPrograms(position, completionTerms, completionParseResult));
        }

        if(!nestedCompletionTerms.isEmpty()) {
            completions
                .addAll(completionErroneousProgramsNested(position, nestedCompletionTerms, completionParseResult));
        }

        if(completionTerms.isEmpty() && nestedCompletionTerms.isEmpty()) {
            completions.addAll(completionCorrectPrograms(position, blankLineCompletion, parseInput));
        }


        return completions;

    }

    public Collection<? extends ICompletion> completionEmptyProgram(Iterable<String> startSymbols, int endOffset,
        ILanguageImpl language, FileObject location) throws MetaborgException {
        Collection<ICompletion> completions = new LinkedList<>();

        final String languageName = language.belongsTo().name();

        for(ILanguageComponent component : language.components()) {
            // call Stratego part of the framework to compute change
            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);

            for(String startSymbol : startSymbols) {
                String placeholderName = startSymbol + "-Plhdr";
                IStrategoAppl placeholder = termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                IStrategoTuple input = termFactory.makeTuple(termFactory.makeString(startSymbol), placeholder);

                final IStrategoTerm proposalsPlaceholder =
                    strategoCommon.invoke(runtime, input, "get-proposals-empty-program-" + languageName);

                if(proposalsPlaceholder == null) {
                    logger.error("Getting proposals for {} failed", placeholder);
                    continue;
                }

                for(IStrategoTerm proposalTerm : proposalsPlaceholder) {
                    if(!(TermUtils.isTuple(proposalTerm))) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
                    if(tuple.getSubtermCount() != 2 || !TermUtils.isStringAt(tuple, 0)
                        || !TermUtils.isStringAt(tuple, 1)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    final String name = TermUtils.toJavaStringAt(tuple, 0);
                    final String text = TermUtils.toJavaStringAt(tuple, 1);
                    final String additionalInfo = TermUtils.toJavaStringAt(tuple, 1);

                    completions.add(new Completion(name, startSymbol, text, additionalInfo, 0, endOffset,
                        CompletionKind.expansion));
                }
            }
        }

        return completions;
    }



    private boolean isCompletionBlankLine(int position, String text) {
        int i = position - 1;
        while(i >= 0) {
            if(text.charAt(i) == '\n') {
                break;
            } else if(text.charAt(i) == ' ' || text.charAt(i) == '\t') {
                i--;
                continue;
            } else
                return false;
        }
        i = position;
        while(i < text.length()) {
            if(text.charAt(i) == '\n') {
                break;
            } else if(text.charAt(i) == ' ' || text.charAt(i) == '\t') {
                i++;
                continue;
            } else
                return false;
        }

        return true;
    }

    public Collection<ICompletion> completionCorrectPrograms(int position, boolean blankLineCompletion,
        ISpoofaxParseUnit parseResult) throws MetaborgException {

        Collection<ICompletion> completions = new HashSet<ICompletion>();
        final FileObject location = parseResult.source();
        final ILanguageImpl language = parseResult.input().langImpl();
        final String languageName = language.belongsTo().name();

        for(ILanguageComponent component : language.components()) {
            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);

            final Map<IStrategoTerm, Boolean> leftRecursiveTerms = new HashMap<IStrategoTerm, Boolean>();
            final Map<IStrategoTerm, Boolean> rightRecursiveTerms = new HashMap<IStrategoTerm, Boolean>();

            final Iterable<IStrategoTerm> terms =
                tracingTermsCompletions(position, parseResult.ast(), new SourceRegion(position), runtime, termFactory,
                    languageName, leftRecursiveTerms, rightRecursiveTerms);

            final IStrategoAppl placeholder = getPlaceholder(position, terms);
            final Iterable<IStrategoList> lists = getLists(terms, leftRecursiveTerms, rightRecursiveTerms);
            final Iterable<IStrategoTerm> optionals = getOptionals(terms, leftRecursiveTerms, rightRecursiveTerms);
            final Iterable<IStrategoTerm> leftRecursive = getLeftRecursiveTerms(position, terms, leftRecursiveTerms);
            final Iterable<IStrategoTerm> rightRecursive = getRightRecursiveTerms(position, terms, rightRecursiveTerms);

            if(placeholder != null) {
                completions.addAll(placeholderCompletions(placeholder, languageName, component, location));
            } else {
                if(Iterables2.size(lists) != 0) {
                    completions.addAll(
                        listsCompletions(position, blankLineCompletion, lists, languageName, component, location));
                }

                if(Iterables2.size(optionals) != 0) {
                    completions
                        .addAll(optionalCompletions(optionals, blankLineCompletion, languageName, component, location));
                }
                // TODO Improve recursive completions
                // if(Iterables2.size(leftRecursive) != 0 || Iterables2.size(rightRecursive) != 0) {
                // completions
                // .addAll(recursiveCompletions(leftRecursive, rightRecursive, languageName, component, location));
                // }
            }
        }
        return completions;
    }

    private Collection<? extends ICompletion> recursiveCompletions(Iterable<IStrategoTerm> leftRecursive,
        Iterable<IStrategoTerm> rightRecursive, String languageName, ILanguageComponent component, FileObject location)
        throws MetaborgException {
        Collection<ICompletion> completions = new LinkedList<>();

        // call Stratego part of the framework to compute change
        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);

        for(IStrategoTerm term : leftRecursive) {
            IStrategoTerm sort = termFactory.makeString(ImploderAttachment.getSort(term));

            final IStrategoTerm strategoInput = termFactory.makeTuple(sort, term);
            IStrategoTerm proposals = null;
            try {
                proposals =
                    strategoCommon.invoke(runtime, strategoInput, "get-proposals-left-recursive-" + languageName);
            } catch(Exception e) {
                logger.error("Getting proposals for {} failed", term);
                continue;
            }
            if(proposals == null) {
                logger.error("Getting proposals for {} failed", term);
                continue;
            }
            for(IStrategoTerm proposalTerm : proposals) {

                final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
                if(tuple.getSubtermCount() != 5 // @formatter:off
                    || !TermUtils.isStringAt(tuple, 0)
                    || !TermUtils.isStringAt(tuple, 1)
                    || !TermUtils.isStringAt(tuple, 2)
                    || !TermUtils.isApplAt(tuple, 3)
                    || !TermUtils.isStringAt(tuple, 4)) { // @formatter:on
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                final String name = TermUtils.toJavaStringAt(tuple, 0);
                final String text = TermUtils.toJavaStringAt(tuple, 1);
                final String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
                final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);
                final String prefix = TermUtils.toJavaStringAt(tuple, 4);

                if(change.getConstructor().getName().contains("REPLACE_TERM")) {
                    final ICompletion completion =
                        createCompletionReplaceTerm(name, text, additionalInfo, change, false, prefix, "");

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                }
            }
        }

        for(IStrategoTerm term : rightRecursive) {
            IStrategoTerm sort = termFactory.makeString(ImploderAttachment.getSort(term));

            final IStrategoTerm strategoInput = termFactory.makeTuple(sort, term);

            IStrategoTerm proposals = null;
            try {
                proposals =
                    strategoCommon.invoke(runtime, strategoInput, "get-proposals-right-recursive-" + languageName);
            } catch(Exception e) {
                logger.error("Getting proposals for {} failed", term);
                continue;
            }
            if(proposals == null) {
                logger.error("Getting proposals for {} failed", term);
                continue;
            }
            for(IStrategoTerm proposalTerm : proposals) {

                final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                if(tuple.getSubtermCount() != 5 // @formatter:off
                    || !(TermUtils.isString(tuple.getSubterm(0)))
                    || !(TermUtils.isString(tuple.getSubterm(1)))
                    || !(TermUtils.isString(tuple.getSubterm(2)))
                    || !(TermUtils.isAppl(tuple.getSubterm(3)))
                    || !(TermUtils.isString(tuple.getSubterm(4)))) { // @formatter:on
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                final String name = TermUtils.toJavaStringAt(tuple, 0);
                final String text = TermUtils.toJavaStringAt(tuple, 1);
                final String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
                final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);
                final String suffix = TermUtils.toJavaStringAt(tuple, 4);

                if(change.getConstructor().getName().contains("REPLACE_TERM")) {
                    final ICompletion completion =
                        createCompletionReplaceTerm(name, text, additionalInfo, change, false, "", suffix);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                }
            }
        }

        return completions;
    }

    public Collection<ICompletion> placeholderCompletions(IStrategoAppl placeholder, String languageName,
        ILanguageComponent component, FileObject location) throws MetaborgException {
        Collection<ICompletion> completions = new LinkedList<>();

        // call Stratego part of the framework to compute change
        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);

        IStrategoTerm placeholderParent = ParentAttachment.getParent(placeholder);
        if(placeholderParent == null) {
            placeholderParent = placeholder;
        }

        IStrategoInt placeholderIdx = termFactory.makeInt(-1);

        for(int i = 0; i < placeholderParent.getSubtermCount(); i++) {
            if(placeholderParent.getSubterm(i) == placeholder) {
                placeholderIdx = termFactory.makeInt(i);
            }
        }

        final String sort = ImploderAttachment.getSort(placeholder);
        final IStrategoTerm strategoInput =
            termFactory.makeTuple(termFactory.makeString(sort), placeholder, placeholderParent, placeholderIdx);

        final IStrategoTerm proposalsPlaceholder =
            strategoCommon.invoke(runtime, strategoInput, "get-proposals-placeholder-" + languageName);

        if(proposalsPlaceholder == null) {
            logger.error("Getting proposals for {} failed", placeholder);
            return completions;
        }
        for(IStrategoTerm proposalTerm : proposalsPlaceholder) {
            if(!(TermUtils.isTuple(proposalTerm))) {
                logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                continue;
            }
            final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
            if(tuple.getSubtermCount() != 4 // @formatter:off
                || !TermUtils.isStringAt(tuple, 0)
                || !TermUtils.isStringAt(tuple, 1)
                || !TermUtils.isStringAt(tuple, 2)
                || !TermUtils.isApplAt(tuple, 3)) { // @formatter:on
                logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                continue;
            }

            final String name = TermUtils.toJavaStringAt(tuple, 0);
            final String text = TermUtils.toJavaStringAt(tuple, 1);
            final String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
            final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);

            if(change.getConstructor().getName().contains("REPLACE_TERM")) {
                final ICompletion completion =
                    createCompletionReplaceTerm(name, text, additionalInfo, change, false, "", "");

                if(completion == null) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                completions.add(completion);
            }
        }

        return completions;
    }

    public Collection<ICompletion> optionalCompletions(Iterable<IStrategoTerm> optionals, boolean blankLineCompletion,
        String languageName, ILanguageComponent component, FileObject location) throws MetaborgException {

        Collection<ICompletion> completions = new LinkedList<>();

        for(IStrategoTerm optional : optionals) {

            ImploderAttachment attachment = optional.getAttachment(ImploderAttachment.TYPE);
            String sort = attachment.getSort().substring(0, attachment.getSort().length());
            String placeholderName = sort + "-Plhdr";
            IStrategoAppl optionalPlaceholder = termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
            final IStrategoTerm strategoInput =
                termFactory.makeTuple(termFactory.makeString(sort), optional, optionalPlaceholder);

            // call Stratego part of the framework to compute change
            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
            final IStrategoTerm proposalsOptional =
                strategoCommon.invoke(runtime, strategoInput, "get-proposals-optional-" + languageName);

            if(proposalsOptional == null) {
                logger.error("Getting proposals for {} failed", strategoInput);
                continue;
            }

            for(IStrategoTerm proposalTerm : proposalsOptional) {
                if(!(TermUtils.isTuple(proposalTerm))) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
                if(tuple.getSubtermCount() != 4 // @formatter:off
                    || !TermUtils.isStringAt(tuple, 0)
                    || !TermUtils.isStringAt(tuple, 1)
                    || !TermUtils.isStringAt(tuple, 2)
                    || !TermUtils.isApplAt(tuple, 2)) { // @formatter:on
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                final String name = TermUtils.toJavaStringAt(tuple, 0);
                final String text = TermUtils.toJavaStringAt(tuple, 1);
                final String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
                final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);

                if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                    final ICompletion completion =
                        createCompletionReplaceTerm(name, text, additionalInfo, change, blankLineCompletion, "", "");

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                }
            }
        }

        return completions;
    }

    public Collection<ICompletion> listsCompletions(int position, boolean blankLineCompletion,
        Iterable<IStrategoList> lists, String languageName, ILanguageComponent component, FileObject location)
        throws MetaborgException {

        Collection<ICompletion> completions = new LinkedList<>();

        for(IStrategoList list : lists) {
            ListImploderAttachment attachment = list.getAttachment(null);
            String sort = attachment.getSort().substring(0, attachment.getSort().length() - 1);
            String placeholderName = sort + "-Plhdr";
            IStrategoAppl listPlaceholder = termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
            final IStrategoTerm strategoInput = termFactory.makeTuple(termFactory.makeString(sort), list,
                listPlaceholder, termFactory.makeInt(position));
            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
            final IStrategoTerm proposalsLists =
                strategoCommon.invoke(runtime, strategoInput, "get-proposals-list-" + languageName);
            if(proposalsLists == null) {
                logger.error("Getting proposals for {} failed", strategoInput);
                continue;
            }
            for(IStrategoTerm proposalTerm : proposalsLists) {
                if(!(TermUtils.isTuple(proposalTerm))) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
                if(tuple.getSubtermCount() != 4 // @formatter:off
                    || !TermUtils.isStringAt(tuple, 0)
                    || !TermUtils.isStringAt(tuple, 1)
                    || !TermUtils.isStringAt(tuple, 2)
                    || !TermUtils.isApplAt(tuple, 3)) { // @formatter:on
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                final String name = TermUtils.toJavaStringAt(tuple, 0);
                final String text = TermUtils.toJavaStringAt(tuple, 1);
                final String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
                final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);


                // if the change is inserting at the end of a list
                if(change.getConstructor().getName().contains("INSERT_AT_END")) {

                    final ICompletion completion =
                        createCompletionInsertAtEnd(name, text, additionalInfo, change, blankLineCompletion);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {

                    final ICompletion completion = createCompletionInsertBefore(name, text, additionalInfo, change);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                }
            }
        }

        return completions;
    }

    private ICompletion createCompletionReplaceTerm(String name, String text, String additionalInfo,
        IStrategoAppl change, boolean blankLineCompletion, String prefix, String suffix) {

        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);

        if(change.getSubtermCount() != 2 || !(TermUtils.isAppl(newNode)) || !(TermUtils.isAppl(oldNode))) {
            return null;
        }

        final String sort = ImploderAttachment.getSort(oldNode);

        int insertionPoint, suffixPoint;

        final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);
        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldNode);

        // check if it's an empty node
        if(oldNodeIA.getLeftToken().getStartOffset() > oldNodeIA.getRightToken().getEndOffset()) {
            // get the last non-layout token before the new node
            int tokenPosition =
                oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
            while(tokenPosition > 0 && (tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR))
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();

            // if completion does not spam multiple lines preserve everything starting at the first non-layout char
            if(!additionalInfo.contains("\n")) {
                tokenPosition = oldNodeIA.getLeftToken().getIndex() + 1 < tokenizer.getTokenCount()
                    ? oldNodeIA.getLeftToken().getIndex() + 1 : tokenizer.getTokenCount() - 1;
                while(tokenPosition < tokenizer.getTokenCount()
                    && (tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                        || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR))
                    tokenPosition++;
                suffixPoint = tokenizer.getTokenAt(tokenPosition).getStartOffset();
            } else { // if completion spams multiple lines keep the lines
                suffixPoint = insertionPoint + 1;
            }
            // if completion is triggered in an empty line, consume that line
            IToken checkToken;
            boolean blankLine = false;
            if(blankLineCompletion) {
                for(; tokenPosition < tokenizer.getTokenCount(); tokenPosition++) {
                    checkToken = tokenizer.getTokenAt(tokenPosition);
                    if(tokenizer.toString(checkToken, checkToken).contains("\n")) {
                        suffixPoint = checkToken.getEndOffset();
                        if(!blankLine) {
                            blankLine = true;
                        } else {
                            break;
                        }
                    }
                }
            }


        } else { // if not, do a regular replacement
            insertionPoint = oldNodeIA.getLeftToken().getStartOffset() - 1;
            suffixPoint = oldNodeIA.getRightToken().getEndOffset() + 1;
        }

        CompletionKind kind;

        if(prefix.equals("") && suffix.equals("")) {
            kind = CompletionKind.expansion;
        } else {
            kind = CompletionKind.expansionEditing;
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint, kind, prefix, suffix);
    }

    private ICompletion createCompletionInsertBefore(String name, String text, String additionalInfo,
        IStrategoAppl change) {

        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);


        // expect two terms and 1st should be an element of a list
        final IStrategoTerm oldList = ParentAttachment.getParent(oldNode);

        if(change.getSubtermCount() != 2 || !(TermUtils.isAppl(oldNode)) || !(TermUtils.isList(newNode))
            || !(TermUtils.isList(oldList))) {
            return null;
        }

        final String sort = ImploderAttachment.getSort(oldNode);

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldNode);

        IStrategoTerm[] subterms = oldList.getAllSubterms();
        int indexOfElement;
        for(indexOfElement = 0; indexOfElement < subterms.length; indexOfElement++) {
            if(subterms[indexOfElement] == oldNode)
                break;
        }

        // if inserted element is first (only two elements)
        if(indexOfElement == 0) {
            // insert after the first non-layout token before the leftmost token of the
            // completion node
            final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);
            int tokenPosition =
                oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR) && tokenPosition > 0)
                tokenPosition--;

            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if inserted element is not first
            // insert after at end offset of the rightmost token of the element before the
            // completion
            IStrategoTerm elementBefore = oldList.getSubterm(indexOfElement - 1);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
        }

        // if completion is separated by a newline, preserve indentation of the subsequent node
        // else separation follows from the grammar
        String separator = "";
        for(int i = text.length() - 1; i >= 0; i--) {
            if(text.charAt(i) == additionalInfo.charAt(additionalInfo.length() - 1)) {
                break;
            }
            separator = text.charAt(i) + separator;
        }

        IToken checkToken = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken();
        int checkTokenIdx = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getIndex();
        suffixPoint = insertionPoint;
        if(separator.contains("\n")) {
            for(; checkTokenIdx >= 0; checkTokenIdx--) {
                checkToken = tokenizer.getTokenAt(checkTokenIdx);
                if(tokenizer.toString(checkToken, checkToken).contains("\n")) {
                    break;
                }
                suffixPoint = checkToken.getStartOffset();
            }
        } else {
            suffixPoint = checkToken.getStartOffset();
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint,
            CompletionKind.expansion);
    }


    private ICompletion createCompletionInsertAtEnd(String name, String text, String additionalInfo,
        IStrategoAppl change, boolean blankLineCompletion) {

        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);

        // expected two lists
        if(change.getSubtermCount() != 2 || !TermUtils.isList(oldNode) || !TermUtils.isList(newNode)) {
            return null;
        }

        final String sort = ImploderAttachment.getElementSort(oldNode);

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldNode);
        final ImploderAttachment oldListIA = oldNode.getAttachment(ImploderAttachment.TYPE);
        int tokenPosition;
        // if list is empty
        // insert after the first non-layout token before the leftmost token of the completion
        // node
        if(oldNode.getSubtermCount() == 0) {
            tokenPosition = oldListIA.getLeftToken().getIndex() - 1 > 0 ? oldListIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR) && tokenPosition > 0)
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if list is not empty
            // insert after at end offset of the rightmost token of the element before the
            // completion
            IStrategoTerm elementBefore = oldNode.getSubterm(oldNode.getAllSubterms().length - 1);
            int leftIdx = elementBefore.getAttachment(ImploderAttachment.TYPE).getLeftToken().getIndex();
            int rightIdx = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getIndex();
            while((tokenizer.getTokenAt(rightIdx).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(rightIdx).getLength() == 0) && rightIdx > leftIdx) {
                rightIdx--;
            }
            insertionPoint = tokenizer.getTokenAt(rightIdx).getEndOffset();
            tokenPosition = rightIdx;
        }
        suffixPoint = insertionPoint + 1;

        // if completion is triggered in an empty line, consume that line
        IToken checkToken;
        boolean blankLine = false;
        if(blankLineCompletion) {
            for(; tokenPosition < tokenizer.getTokenCount(); tokenPosition++) {
                checkToken = tokenizer.getTokenAt(tokenPosition);
                if(tokenizer.toString(checkToken, checkToken).contains("\n")) {
                    suffixPoint = checkToken.getEndOffset();
                    if(!blankLine) {
                        blankLine = true;
                    } else {
                        break;
                    }
                }
            }
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint,
            CompletionKind.expansion);
    }

    public Collection<ICompletion> completionErroneousPrograms(int cursorPosition,
        Iterable<IStrategoTerm> completionTerms, ISpoofaxParseUnit completionParseResult) throws MetaborgException {

        final FileObject location = completionParseResult.source();
        final ILanguageImpl language = completionParseResult.input().langImpl();
        final String languageName = language.belongsTo().name();
        final Collection<ICompletion> completions = new LinkedList<>();
        final Collection<IStrategoTerm> proposalsTerm = new LinkedList<>();

        for(ILanguageComponent component : language.components()) {
            for(IStrategoTerm completionTerm : completionTerms) {
                IStrategoTerm completionAst = completionParseResult.ast();
                final IStrategoTerm topMostAmb = findTopMostAmbNode(completionTerm);

                if(ImploderAttachment.get(completionTerm).isSinglePlaceholderCompletion()) {
                    Collection<IStrategoTerm> placeholders = new LinkedList<>();
                    placeholders.addAll(findPlaceholderTerms(completionTerm));
                    if(placeholders.size() != 1) {
                        logger.error("Getting proposals for {} failed", completionTerm);
                        continue;
                    }

                    IStrategoAppl placeholderTerm = (IStrategoAppl) placeholders.iterator().next();
                    IStrategoAppl placeholder = termFactory
                        .makeAppl(termFactory.makeConstructor(placeholderTerm.getConstructor().getName(), 0));


                    IStrategoTerm parenthesized = parenthesizeTerm(completionTerm, termFactory);
                    final IStrategoTerm inputStratego =
                        termFactory.makeTuple(termFactory.makeString(ImploderAttachment.getElementSort(parenthesized)),
                            completionAst, completionTerm, topMostAmb, parenthesized, placeholder, placeholderTerm);

                    final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                    final IStrategoTerm proposalTerm = strategoCommon.invoke(runtime, inputStratego,
                        "get-proposals-incorrect-programs-single-placeholder-" + languageName);
                    if(proposalTerm == null || !(TermUtils.isList(proposalTerm))) {
                        logger.error("Getting proposals for {} failed", completionTerm);
                        continue;
                    }

                    for(IStrategoTerm proposalPlaceholder : proposalTerm) {
                        proposalsTerm.add(proposalPlaceholder);
                    }

                } else {

                    IStrategoTerm parenthesized = parenthesizeTerm(completionTerm, termFactory);
                    final IStrategoTerm inputStratego =
                        termFactory.makeTuple(termFactory.makeString(ImploderAttachment.getElementSort(parenthesized)),
                            completionAst, completionTerm, topMostAmb, parenthesized);

                    final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                    final IStrategoTerm proposalTerm = strategoCommon.invoke(runtime, inputStratego,
                        "get-proposals-incorrect-programs-" + languageName);
                    if(proposalTerm == null) {
                        logger.error("Getting proposals for {} failed", completionTerm);
                        continue;
                    }

                    proposalsTerm.add(proposalTerm);
                }
            }

            for(IStrategoTerm proposalTerm : proposalsTerm) {
                if(!(TermUtils.isTuple(proposalTerm))) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final IStrategoTuple tuple = TermUtils.toTuple(proposalTerm);
                if(tuple.getSubtermCount() != 6 // @formatter:off
                    || !TermUtils.isStringAt(tuple, 0)
                    || !TermUtils.isStringAt(tuple, 1)
                    || !TermUtils.isStringAt(tuple, 2)
                    || !TermUtils.isApplAt(tuple, 3)
                    || (tuple.getSubterm(4) == null)
                    || !TermUtils.isStringAt(tuple, 5)) { // @formatter:on
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final String name = TermUtils.toJavaStringAt(tuple, 0);
                String text = TermUtils.toJavaStringAt(tuple, 1);
                String additionalInfo = TermUtils.toJavaStringAt(tuple, 2);
                final IStrategoAppl change = TermUtils.toApplAt(tuple, 3);
                final IStrategoTerm completionTerm = tuple.getSubterm(4);
                final String completionKind = TermUtils.toJavaStringAt(tuple, 5);
                String prefix = calculatePrefix(cursorPosition, completionTerm);
                String suffix = calculateSuffix(cursorPosition, completionTerm);

                // if the change is inserting at the end of a list
                if(change.getConstructor().getName().contains("INSERT_AT_END")) {

                    // calls a different method because now, the program has errors that should be fixed
                    final ICompletion completion = createCompletionInsertAtEndFixing(name, text, additionalInfo, prefix,
                        suffix, change, completionKind);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {

                    final ICompletion completion = createCompletionInsertBeforeFixing(name, text, additionalInfo,
                        prefix, suffix, change, completionKind);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);


                } else if(change.getConstructor().getName().contains("INSERTION_TERM")) {

                    final ICompletion completion = createCompletionInsertionTermFixing(name, text, additionalInfo,
                        prefix, suffix, change, completionKind);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                } else if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                    final ICompletion completion = createCompletionReplaceTermFixing(name, text, additionalInfo, prefix,
                        suffix, change, completionKind);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                }
            }
        }

        return completions;
    }

    private String calculatePrefix(int cursorPosition, IStrategoTerm proposalTerm) {

        String prefix = "";
        IToken leftToken = proposalTerm.getAttachment(ImploderAttachment.TYPE).getLeftToken();
        IToken rightToken = proposalTerm.getAttachment(ImploderAttachment.TYPE).getRightToken();
        ITokenizer tokenizer = (ITokenizer) leftToken.getTokenizer();
        IToken current = leftToken;
        int endOffsetPrefix = Integer.MIN_VALUE;
        while(current.getEndOffset() < cursorPosition && current != rightToken) {
            if(endOffsetPrefix < current.getEndOffset()) {
                prefix += current.toString();
                endOffsetPrefix = current.getEndOffset();
            }
            current = tokenizer.getTokenAt(current.getIndex() + 1);
        }

        return prefix;
    }


    private String calculateSuffix(int cursorPosition, IStrategoTerm proposalTerm) {

        String suffix = "";
        IToken leftToken = proposalTerm.getAttachment(ImploderAttachment.TYPE).getLeftToken();
        IToken rightToken = proposalTerm.getAttachment(ImploderAttachment.TYPE).getRightToken();
        ITokenizer tokenizer = (ITokenizer) leftToken.getTokenizer();
        IToken current = rightToken;
        int startOffsetSuffix = Integer.MAX_VALUE;
        while(current.getStartOffset() >= cursorPosition && current != leftToken) {
            if(startOffsetSuffix > current.getStartOffset()) {
                suffix = current.toString() + suffix;
                startOffsetSuffix = current.getStartOffset();
            }
            current = tokenizer.getTokenAt(current.getIndex() - 1);
        }

        return suffix;
    }

    private ICompletion createCompletionInsertionTermFixing(String name, String text, String additionalInfo,
        String prefix, String suffix, IStrategoAppl change, String completionKind) {
        final IStrategoTerm newNode = change.getSubterm(0);


        if(change.getSubtermCount() != 1 || !(TermUtils.isAppl(newNode))) {
            return null;
        }

        final String sort = ImploderAttachment.getSort(newNode);

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(newNode);

        final IStrategoTerm topMostAmb = findTopMostAmbNode(newNode);
        final ImploderAttachment topMostAmbIA = topMostAmb.getAttachment(ImploderAttachment.TYPE);

        // get the last non-layout token before the topmost ambiguity
        int tokenPosition =
            topMostAmbIA.getLeftToken().getIndex() - 1 > 0 ? topMostAmbIA.getLeftToken().getIndex() - 1 : 0;
        while((tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
            || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR) && tokenPosition > 0)
            tokenPosition--;

        insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();

        if(topMostAmbIA.getRightToken().getEndOffset() < topMostAmbIA.getRightToken().getStartOffset()) {
            // keep all the characters after the last non-layout token if completion ends with a
            // placeholder
            tokenPosition = topMostAmbIA.getRightToken().getIndex();
            while(tokenPosition > 0
                && (tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(tokenPosition)
                    .getStartOffset() || tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT))
                tokenPosition--;
            suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;

        } else {
            // skip all the (erroneous) characters that were in the text already
            suffixPoint = topMostAmbIA.getRightToken().getEndOffset() + 1;
        }

        CompletionKind kind;
        if(completionKind.equals("recovery")) {
            kind = CompletionKind.recovery;
        } else if(completionKind.equals("expansionEditing")) {
            kind = CompletionKind.expansionEditing;
        } else {
            kind = CompletionKind.expansion;
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint, kind, prefix, suffix);
    }


    private ICompletion createCompletionInsertBeforeFixing(String name, String text, String additionalInfo,
        String prefix, String suffix, IStrategoAppl change, String completionKind) {

        // expect two terms and 1st should be an element of a list
        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);
        final IStrategoTerm oldList = ParentAttachment.getParent(oldNode);

        if(change.getSubtermCount() != 2 || !(TermUtils.isAppl(oldNode)) || !(TermUtils.isAppl(newNode))
            || !(TermUtils.isList(oldList))) {
            return null;
        }

        final String sort = ImploderAttachment.getSort(oldNode);

        int insertionPoint, suffixPoint;

        IStrategoTerm[] subterms = TermUtils.toList(oldList).getAllSubterms();
        int indexOfCompletion;
        for(indexOfCompletion = 0; indexOfCompletion < subterms.length; indexOfCompletion++) {
            if(subterms[indexOfCompletion] == oldNode)
                break;
        }
        // if inserted element is first (only two elements)
        if(indexOfCompletion == 1) {
            // insert after the first non-layout token before the leftmost token of the list
            ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldList);

            // to avoid keeping duplicate tokens due to ambiguity
            IStrategoTerm topMostAmbOldList = findTopMostAmbNode(oldList);
            final ImploderAttachment oldListIA = topMostAmbOldList.getAttachment(ImploderAttachment.TYPE);

            int tokenPosition =
                oldListIA.getLeftToken().getIndex() - 1 > 0 ? oldListIA.getLeftToken().getIndex() - 1 : 0;
            while((checkEmptyOffset(tokenizer.getTokenAt(tokenPosition))
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR) && tokenPosition > 0)
                tokenPosition--;

            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if inserted element is not first
            // insert after at end offset of the rightmost token of the element before the completion
            IStrategoTerm elementBefore = oldList.getSubterm(indexOfCompletion - 2);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();

        }
        // suffix point should be the first token of the next element
        suffixPoint = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getStartOffset();

        CompletionKind kind;
        if(completionKind.equals("recovery")) {
            kind = CompletionKind.recovery;
        } else if(completionKind.equals("expansionEditing")) {
            kind = CompletionKind.expansionEditing;
        } else {
            kind = CompletionKind.expansion;
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint, kind, prefix, suffix);
    }


    private ICompletion createCompletionInsertAtEndFixing(String name, String text, String additionalInfo,
        String prefix, String suffix, IStrategoAppl change, String completionKind) {

        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);

        final IStrategoTerm oldNodeTopMostAmb = findTopMostAmbNode(oldNode);


        if(change.getSubtermCount() != 2 || !(TermUtils.isList(oldNode)) || !(TermUtils.isAppl(newNode))) {
            return null;
        }

        final String sort = ImploderAttachment.getElementSort(oldNode);

        int insertionPoint, suffixPoint;
        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldNodeTopMostAmb);
        final ImploderAttachment oldNodeIA = oldNodeTopMostAmb.getAttachment(ImploderAttachment.TYPE);

        // if list is empty
        // insert after the first non-layout token before the leftmost token of the completion node
        if(((IStrategoList) oldNode).size() == 1) {
            int tokenPosition =
                oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
            while(tokenPosition > 0 && (checkEmptyOffset(tokenizer.getTokenAt(tokenPosition))
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR))
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if list is not empty
            // insert after at end offset of the rightmost token of the element before the completion
            IStrategoTerm elementBefore = oldNode.getSubterm(oldNode.getAllSubterms().length - 2);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
        }

        // keep all the characters after the last non-layout token
        int tokenPosition = oldNodeIA.getRightToken().getIndex();
        while(tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(tokenPosition).getStartOffset()
            || tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT && tokenPosition > 0)
            tokenPosition--;
        suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;


        CompletionKind kind;
        if(completionKind.equals("recovery")) {
            kind = CompletionKind.recovery;
        } else if(completionKind.equals("expansionEditing")) {
            kind = CompletionKind.expansionEditing;
        } else {
            kind = CompletionKind.expansion;
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint, kind, prefix, suffix);
    }

    private boolean isCompletionNode(ISimpleTerm term) {
        if(term == null)
            return false;

        if(ImploderAttachment.get(term).isCompletion() || ImploderAttachment.get(term).isNestedCompletion())
            return true;

        return false;
    }

    public Collection<? extends ICompletion> completionErroneousProgramsNested(int cursorPosition,
        Collection<IStrategoTerm> nestedCompletionTerms, ISpoofaxParseUnit completionParseResult)
        throws MetaborgException {
        final FileObject location = completionParseResult.source();
        final ILanguageImpl language = completionParseResult.input().langImpl();
        final String languageName = language.belongsTo().name();
        final Collection<ICompletion> completions = new LinkedList<>();
        IStrategoTerm completionAst = completionParseResult.ast();

        for(ILanguageComponent component : language.components()) {
            for(IStrategoTerm nestedCompletionTerm : nestedCompletionTerms) {
                final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);

                Collection<IStrategoTerm> inputsStrategoNested = new LinkedList<>();

                // calculate direct proposals
                inputsStrategoNested.addAll(calculateDirectCompletionProposals(nestedCompletionTerm, termFactory,
                    completionAst, languageName, runtime));

                // calculate inner nested proposals
                Collection<IStrategoTerm> innerNestedCompletionTerms =
                    findNestedCompletionTerm((StrategoTerm) nestedCompletionTerm, true);

                for(IStrategoTerm innerNestedCompletionTerm : innerNestedCompletionTerms) {
                    inputsStrategoNested.addAll(calculateNestedCompletionProposals(nestedCompletionTerm,
                        innerNestedCompletionTerm, termFactory, completionAst, languageName, runtime));
                }

                for(IStrategoTerm inputStrategoNested : inputsStrategoNested) {
                    final IStrategoTerm proposalTermNested = strategoCommon.invoke(runtime, inputStrategoNested,
                        "get-proposals-incorrect-programs-nested-" + languageName);
                    if(proposalTermNested == null) {
                        logger.error("Getting proposals for {} failed", inputStrategoNested);
                        continue;
                    }

                    final String name = TermUtils.toJavaStringAt(proposalTermNested, 0);
                    final String text = TermUtils.toJavaStringAt(proposalTermNested, 1);
                    final String additionalInfo = TermUtils.toJavaStringAt(proposalTermNested, 2);
                    final IStrategoAppl change = TermUtils.toApplAt(proposalTermNested, 3);
                    final IStrategoTerm completionTerm = proposalTermNested.getSubterm(4);
                    String prefix = calculatePrefix(cursorPosition, completionTerm);
                    String suffix = calculateSuffix(cursorPosition, completionTerm);
                    String completionKind = "recovery";

                    // if the change is inserting at the end of a list
                    if(change.getConstructor().getName().contains("INSERT_AT_END")) {

                        // calls a different method because now, the program has errors that should be fixed
                        final ICompletion completion = createCompletionInsertAtEndFixing(name, text, additionalInfo,
                            prefix, suffix, change, completionKind);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTermNested);
                            continue;
                        }

                        completions.add(completion);
                    } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {

                        final ICompletion completion = createCompletionInsertBeforeFixing(name, text, additionalInfo,
                            prefix, suffix, change, completionKind);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTermNested);
                            continue;
                        }

                        completions.add(completion);


                    } else if(change.getConstructor().getName().contains("INSERTION_TERM")) {

                        final ICompletion completion = createCompletionInsertionTermFixing(name, text, additionalInfo,
                            prefix, suffix, change, completionKind);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTermNested);
                            continue;
                        }

                        completions.add(completion);
                    } else if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                        final ICompletion completion = createCompletionReplaceTermFixing(name, text, additionalInfo,
                            prefix, suffix, change, completionKind);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTermNested);
                            continue;
                        }

                        completions.add(completion);
                    }
                }
            }
        }

        return completions;
    }


    private Collection<IStrategoTerm> calculateNestedCompletionProposals(IStrategoTerm mainNestedCompletionTerm,
        IStrategoTerm nestedCompletionTerm, ITermFactory termFactory, IStrategoTerm completionAst, String languageName,
        HybridInterpreter runtime) throws MetaborgException {
        Collection<IStrategoTerm> inputsStratego = new LinkedList<>();

        Collection<IStrategoTerm> nestedCompletionTerms = findNestedCompletionTerm(nestedCompletionTerm, true);

        for(IStrategoTerm innerNestedCompletionTerm : nestedCompletionTerms) {
            Collection<IStrategoTerm> inputsStrategoInnerNested = calculateNestedCompletionProposals(
                nestedCompletionTerm, innerNestedCompletionTerm, termFactory, completionAst, languageName, runtime);
            for(IStrategoTerm inputStrategoNested : inputsStrategoInnerNested) {
                final IStrategoTerm proposalTermNested = strategoCommon.invoke(runtime, inputStrategoNested,
                    "get-proposals-incorrect-programs-nested-" + languageName);
                if(proposalTermNested == null) {
                    logger.error("Getting proposals for {} failed", inputStrategoNested);
                    continue;
                }
                final IStrategoTerm topMostAmb = findTopMostAmbNode(nestedCompletionTerm);
                final IStrategoTerm replaceTermText = termFactory.makeAppl(
                    new StrategoConstructor("REPLACE_TERM_TEXT", 2), topMostAmb, proposalTermNested.getSubterm(1));

                IStrategoTerm parenthesized = parenthesizeTerm(mainNestedCompletionTerm, termFactory);
                final IStrategoTerm inputStrategoInnerNested = termFactory.makeTuple(
                    termFactory.makeString(ImploderAttachment.getElementSort(parenthesized)), completionAst,
                    mainNestedCompletionTerm, proposalTermNested.getSubterm(0), replaceTermText, parenthesized);

                inputsStratego.add(inputStrategoInnerNested);
            }

        }

        Collection<IStrategoTerm> inputsStrategoInner =
            calculateDirectCompletionProposals(nestedCompletionTerm, termFactory, completionAst, languageName, runtime);

        for(IStrategoTerm inputStrategoNested : inputsStrategoInner) {
            final IStrategoTerm proposalTermNested = strategoCommon.invoke(runtime, inputStrategoNested,
                "get-proposals-incorrect-programs-nested-" + languageName);
            if(proposalTermNested == null) {
                logger.error("Getting proposals for {} failed", inputStrategoNested);
                continue;
            }
            final IStrategoTerm topMostAmb = findTopMostAmbNode(nestedCompletionTerm);
            final IStrategoTerm replaceTermText = termFactory.makeAppl(new StrategoConstructor("REPLACE_TERM_TEXT", 2),
                topMostAmb, proposalTermNested.getSubterm(1));

            IStrategoTerm parenthesized = parenthesizeTerm(mainNestedCompletionTerm, termFactory);
            final IStrategoTerm inputStrategoInnerNested = termFactory.makeTuple(
                termFactory.makeString(ImploderAttachment.getElementSort(parenthesized)), completionAst,
                mainNestedCompletionTerm, proposalTermNested.getSubterm(0), replaceTermText, parenthesized);

            inputsStratego.add(inputStrategoInnerNested);
        }

        return inputsStratego;
    }


    private Collection<IStrategoTerm> calculateDirectCompletionProposals(IStrategoTerm nestedCompletionTerm,
        ITermFactory termFactory, IStrategoTerm completionAst, String languageName, HybridInterpreter runtime)
        throws MetaborgException {

        Collection<IStrategoTerm> inputsStratego = new LinkedList<>();
        Collection<IStrategoTerm> completionTerms = findCompletionTermInsideNested(nestedCompletionTerm);

        for(IStrategoTerm completionTerm : completionTerms) {
            final IStrategoTerm topMostCompletionTerm = findTopMostCompletionNode(completionTerm);
            final IStrategoTerm topMostAmb = findTopMostAmbNode(topMostCompletionTerm);

            IStrategoTerm parenthesized = parenthesizeTerm(topMostCompletionTerm, termFactory);

            final IStrategoTerm inputStratego =
                termFactory.makeTuple(termFactory.makeString(ImploderAttachment.getElementSort(parenthesized)),
                    completionAst, completionTerm, topMostAmb, parenthesized);

            final IStrategoTerm proposalTerm =
                strategoCommon.invoke(runtime, inputStratego, "get-proposals-incorrect-programs-" + languageName);
            if(proposalTerm == null) {
                logger.error("Getting proposals for {} failed", inputStratego);
                continue;
            }

            final IStrategoTerm replaceTermText = termFactory.makeAppl(new StrategoConstructor("REPLACE_TERM_TEXT", 2),
                topMostAmb, proposalTerm.getSubterm(1));

            IStrategoTerm parenthesizedNested = parenthesizeTerm(nestedCompletionTerm, termFactory);
            final IStrategoTerm inputStrategoNested = termFactory.makeTuple(
                termFactory.makeString(ImploderAttachment.getElementSort(parenthesizedNested)), completionAst,
                nestedCompletionTerm, proposalTerm.getSubterm(0), replaceTermText, parenthesizedNested);

            inputsStratego.add(inputStrategoNested);
        }

        return inputsStratego;
    }

    private IStrategoTerm parenthesizeTerm(IStrategoTerm completionTerm, ITermFactory termFactory) {
        if(ImploderAttachment.get(completionTerm).isBracket()) {
            IStrategoTerm result =
                termFactory.makeAppl(termFactory.makeConstructor("Parenthetical", 1), completionTerm);
            return result;
        }
        return completionTerm;
    }


    private ICompletion createCompletionReplaceTermFixing(String name, String text, String additionalInfo,
        String prefix, String suffix, IStrategoAppl change, String completionKind) {
        final IStrategoTerm oldNode = change.getSubterm(0);
        final IStrategoTerm newNode = change.getSubterm(1);


        if(change.getSubtermCount() != 2 || !(TermUtils.isAppl(newNode)) || !(TermUtils.isAppl(oldNode))) {
            return null;
        }

        final String sort = ImploderAttachment.getSort(oldNode);

        int insertionPoint, suffixPoint;

        final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);
        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(oldNode);

        // check if it's an empty node
        if(oldNodeIA.getLeftToken().getStartOffset() > oldNodeIA.getRightToken().getEndOffset()) {
            // get the last non-layout token before the new node
            int tokenPosition =
                oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == TK_LAYOUT
                || tokenizer.getTokenAt(tokenPosition).getKind() == TK_ERROR) && tokenPosition > 0)
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else { // if not, do a regular replacement
            insertionPoint = oldNodeIA.getLeftToken().getStartOffset() - 1;
        }


        // insert after the first non-layout token
        int tokenPositionEnd = oldNodeIA.getRightToken().getIndex();

        while((tokenizer.getTokenAt(tokenPositionEnd).getEndOffset() < tokenizer.getTokenAt(tokenPositionEnd)
            .getStartOffset() || tokenizer.getTokenAt(tokenPositionEnd).getKind() == TK_LAYOUT
            || tokenizer.getTokenAt(tokenPositionEnd).getKind() == TK_ERROR) && tokenPositionEnd > 0)
            tokenPositionEnd--;

        suffixPoint = tokenizer.getTokenAt(tokenPositionEnd).getEndOffset() + 1;

        CompletionKind kind;
        if(completionKind.equals("recovery")) {
            kind = CompletionKind.recovery;
        } else {
            kind = CompletionKind.expansion;
        }

        return new Completion(name, sort, text, additionalInfo, insertionPoint + 1, suffixPoint, kind, prefix, suffix);
    }

    private boolean checkEmptyOffset(IToken token) {
        if(token.getStartOffset() > token.getEndOffset())
            return true;

        return false;
    }

    private @Nullable IStrategoAppl getPlaceholder(int position, final Iterable<IStrategoTerm> terms) {
        for(IStrategoTerm term : terms) {
            if(TermUtils.isAppl(term)) {
                IToken left = ImploderAttachment.getLeftToken(term);
                IToken right = ImploderAttachment.getRightToken(term);

                final IStrategoAppl appl = (IStrategoAppl) term;
                if(appl.getConstructor().getName().endsWith("-Plhdr") && position > left.getStartOffset()
                    && position <= right.getEndOffset()) {
                    return appl;
                }
            }
        }

        return null;
    }

    private @Nullable Iterable<IStrategoList> getLists(final Iterable<IStrategoTerm> terms,
        Map<IStrategoTerm, Boolean> leftRecursiveTerms, Map<IStrategoTerm, Boolean> rightRecursiveTerms) {

        Collection<IStrategoList> lists = new LinkedList<>();
        for(IStrategoTerm term : terms) {
            if(TermUtils.isList(term)) {
                final IStrategoList list = (IStrategoList) term;
                lists.add(list);
            } else {
                IToken left = ImploderAttachment.getLeftToken(term);
                IToken right = ImploderAttachment.getRightToken(term);
                // if term is not nullable, nor a list nor left or right recursive stop the search
                if(left.getStartOffset() <= right.getEndOffset()) {
                    boolean isLeftRecursive = leftRecursiveTerms.containsKey(term);
                    boolean isRightRecursive = rightRecursiveTerms.containsKey(term);
                    if(!isLeftRecursive && !isRightRecursive) {
                        break;
                    }
                }
            }
        }

        return lists;
    }

    private @Nullable Iterable<IStrategoTerm> getOptionals(final Iterable<IStrategoTerm> terms,
        Map<IStrategoTerm, Boolean> leftRecursiveTerms, Map<IStrategoTerm, Boolean> rightRecursiveTerms) {

        Collection<IStrategoTerm> optionals = new LinkedList<>();
        for(IStrategoTerm term : terms) {
            IToken left = ImploderAttachment.getLeftToken(term);
            IToken right = ImploderAttachment.getRightToken(term);
            if(!(TermUtils.isList(term)) && left.getStartOffset() > right.getEndOffset()) {
                optionals.add(term);
            } else if(TermUtils.isList(term)) {
                continue;
                // if term is not nullable, nor a list nor left or right recursive stop the search
            } else {
                boolean isLeftRecursive = leftRecursiveTerms.containsKey(term);
                boolean isRightRecursive = rightRecursiveTerms.containsKey(term);
                if(!isLeftRecursive && !isRightRecursive) {
                    break;
                }
            }
        }

        return optionals;
    }

    private Iterable<IStrategoTerm> getRightRecursiveTerms(int position, Iterable<IStrategoTerm> terms,
        Map<IStrategoTerm, Boolean> rightRecursiveTerms) {

        Collection<IStrategoTerm> rightRecursive = new LinkedList<>();
        for(IStrategoTerm term : terms) {
            boolean isRightRecursive = rightRecursiveTerms.containsKey(term);

            IToken left = ImploderAttachment.getLeftToken(term);
            IToken right = ImploderAttachment.getRightToken(term);

            if(isRightRecursive && position <= left.getStartOffset()) {
                rightRecursive.add(term);
            } else if(TermUtils.isList(term) || left.getStartOffset() > right.getEndOffset()) {
                continue;
            } else {
                break;
            }
        }

        return rightRecursive;
    }

    private Iterable<IStrategoTerm> getLeftRecursiveTerms(int position, Iterable<IStrategoTerm> terms,
        Map<IStrategoTerm, Boolean> leftRecursiveTerms) {
        Collection<IStrategoTerm> leftRecursive = new LinkedList<>();
        for(IStrategoTerm term : terms) {
            boolean isLeftRecursive = leftRecursiveTerms.containsKey(term);

            IToken left = ImploderAttachment.getLeftToken(term);
            IToken right = ImploderAttachment.getRightToken(term);

            if(isLeftRecursive && position > right.getEndOffset()) {
                leftRecursive.add(term);
            } else if(TermUtils.isList(term) || left.getStartOffset() > right.getEndOffset()) {
                continue;
            } else {
                break;
            }
        }

        return leftRecursive;
    }

    private Collection<IStrategoTerm> getCompletionTermsFromAST(ISpoofaxParseUnit completionParseResult) {

        if(completionParseResult == null) {
            return new LinkedList<>();
        }

        StrategoTerm ast = (StrategoTerm) completionParseResult.ast();

        if(ast == null) {
            return new LinkedList<>();
        }

        Collection<IStrategoTerm> completionTerm = findCompletionTerm(ast);

        return completionTerm;
    }

    private Collection<IStrategoTerm> getNestedCompletionTermsFromAST(ISpoofaxParseUnit completionParseResult) {
        if(completionParseResult == null) {
            return new LinkedList<>();
        }

        StrategoAppl ast = (StrategoAppl) completionParseResult.ast();

        if(ast == null) {
            return new LinkedList<>();
        }

        Collection<IStrategoTerm> completionTerm = findNestedCompletionTerm(ast, false);

        return completionTerm;
    }

    private Iterable<IStrategoTerm> tracingTermsCompletions(final int position, Object result,
        final ISourceRegion region, final HybridInterpreter runtime, final ITermFactory termFactory,
        final String languageName, final Map<IStrategoTerm, Boolean> leftRecursiveTerms,
        final Map<IStrategoTerm, Boolean> rightRecursiveTerms) {
        if(result == null || region == null) {
            return Iterables2.empty();
        }
        final Collection<IStrategoTerm> parsed = new LinkedList<>();

        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                final ISourceLocation location = fromTokens(term, runtime, termFactory, position, languageName,
                    leftRecursiveTerms, rightRecursiveTerms);
                if(location != null && location.region().contains(region)) {
                    parsed.add(term);
                    return false;
                }
                return true;
            }
        };

        StrategoTermVisitee.bottomup(visitor, (IStrategoTerm) result);
        return parsed;
    }

    // TODO: Do these strategies need to be specific for a language
    protected @Nullable ISourceLocation fromTokens(IStrategoTerm fragment, HybridInterpreter runtime,
        ITermFactory termFactory, int position, String languageName, Map<IStrategoTerm, Boolean> leftRecursiveTerms,
        Map<IStrategoTerm, Boolean> rightRecursiveTerms) {
        final FileObject resource = SourceAttachment.getResource(fragment, resourceService);
        final IToken left = ImploderAttachment.getLeftToken(fragment);
        final IToken right = ImploderAttachment.getRightToken(fragment);
        ITokenizer tokenizer = (ITokenizer) ImploderAttachment.getTokenizer(fragment);
        IToken leftmostValid = left;
        IToken rightmostValid = right;
        boolean isList = TermUtils.isList(fragment);
        boolean isOptional = false;
        String sort = ImploderAttachment.getSort(fragment);
        IStrategoTerm input = termFactory.makeString(sort);
        boolean isLeftRecursive = false;

        if(TermUtils.isAppl(fragment) && position > right.getEndOffset()) {
            try {
                isLeftRecursive = strategoCommon.invoke(runtime, input, "is-left-recursive") != null;
            } catch(MetaborgException e) {
                logger.error(
                    "Failed to check recursivity for term {} of sort {} - syntactic completion not activated for this language, please import the completion stratego library",
                    fragment, sort);
            }
        }
        boolean isRightRecursive = false;

        if(TermUtils.isAppl(fragment) && position <= left.getStartOffset()) {
            try {
                isRightRecursive = strategoCommon.invoke(runtime, input, "is-right-recursive") != null;
            } catch(MetaborgException e) {
                logger.error(
                    "Failed to check recursivity for term {} of sort {} - syntactic completion not activated for this language, please import the completion stratego library",
                    fragment, sort);
            }
        }

        if(isLeftRecursive) {
            leftRecursiveTerms.put(fragment, true);
        }

        if(isRightRecursive) {
            rightRecursiveTerms.put(fragment, true);
        }

        if(left == null || right == null) {
            return null;
        }

        if(!isList && left == right && left.getEndOffset() < left.getStartOffset()) {
            isOptional = true;
        }

        // if it's a list or a node that is empty make the element includes the surrounding layout tokens
        if(left.getStartOffset() > right.getEndOffset() || isList || isOptional
            || (isLeftRecursive && isRightRecursive)) {
            for(int i = left.getIndex() - 1; i >= 0; i--) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex() + 1; i < tokenizer.getTokenCount(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }
            // if it is left recursive include the layout only on the right
        } else if(isLeftRecursive) {
            for(int i = left.getIndex(); i < right.getIndex(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i + 1);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex() + 1; i < tokenizer.getTokenCount(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }

            // if it is right recursive include the layout only on the left
        } else if(isRightRecursive) {
            for(int i = left.getIndex() - 1; i >= 0; i--) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex(); i > left.getIndex(); i--) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i - 1);
                } else {
                    break;
                }
            }

        }
        // if not make it stripes the surrounding layout tokens
        else {
            for(int i = left.getIndex(); i < right.getIndex(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i + 1);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex(); i > left.getIndex(); i--) {
                if(tokenizer.getTokenAt(i).getKind() == TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i - 1);
                } else {
                    break;
                }
            }
        }

        final ISourceRegion region = JSGLRSourceRegionFactory.fromTokensLayout(leftmostValid, rightmostValid,
            (isOptional || isList || isLeftRecursive || isRightRecursive));

        return new SourceLocation(region, resource);
    }

    private Collection<IStrategoTerm> findCompletionTerm(StrategoTerm ast) {


        final Collection<IStrategoTerm> completionTerms = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {

            @Override public boolean visit(IStrategoTerm term) {
                ImploderAttachment ia = term.getAttachment(ImploderAttachment.TYPE);
                if(ia.isNestedCompletion()) {
                    return false;
                }
                if(ia.isCompletion()) {
                    completionTerms.add(term);
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.topdown(visitor, ast);

        return completionTerms;
    }

    private Collection<IStrategoTerm> findPlaceholderTerms(IStrategoTerm ast) {

        final Collection<IStrategoTerm> placeholderTerms = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {

            @Override public boolean visit(IStrategoTerm term) {
                if(TermUtils.isAppl(term)) {
                    IStrategoAppl appl = (IStrategoAppl) term;
                    if(appl.getConstructor().getName().contains("-Plhdr") && appl.getSubtermCount() > 0) {
                        placeholderTerms.add(appl);
                        return false;
                    }
                }
                return true;
            }
        };
        StrategoTermVisitee.topdown(visitor, ast);

        return placeholderTerms;
    }

    private Collection<IStrategoTerm> findCompletionTermInsideNested(final IStrategoTerm ast) {

        final Collection<IStrategoTerm> completionTerms = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {

            @Override public boolean visit(IStrategoTerm term) {
                ImploderAttachment ia = term.getAttachment(ImploderAttachment.TYPE);
                if(ia.isNestedCompletion() && !term.equals(ast)) {
                    return false;
                }
                if(ia.isCompletion()) {
                    completionTerms.add(term);
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.topdown(visitor, ast);

        return completionTerms;
    }

    private Collection<IStrategoTerm> findNestedCompletionTerm(final IStrategoTerm ast, final boolean excludeIdTerm) {
        final Collection<IStrategoTerm> completionTerms = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {

            @Override public boolean visit(IStrategoTerm term) {

                ImploderAttachment ia = term.getAttachment(ImploderAttachment.TYPE);
                if(excludeIdTerm && term.equals(ast)) {
                    return true;
                }
                if(ia.isNestedCompletion()) {
                    completionTerms.add(term);
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.topdown(visitor, ast);

        return completionTerms;
    }

    private IStrategoTerm findTopMostAmbNode(IStrategoTerm newNode) {
        IStrategoTerm parent = ParentAttachment.getParent(newNode);
        if(parent == null) {
            return newNode;
        }
        if(ImploderAttachment.getSort(parent) == null)
            return findTopMostAmbNode(parent);

        return newNode;
    }

    private IStrategoTerm findTopMostCompletionNode(IStrategoTerm newNode) {
        IStrategoTerm parent = ParentAttachment.getParent(newNode);
        if(parent == null) {
            return newNode;
        }

        ImploderAttachment ia = ImploderAttachment.get(parent);

        if(ia.getSort() == null || ia.isNestedCompletion()) {
            return newNode;
        }

        return findTopMostCompletionNode(parent);

    }

}
