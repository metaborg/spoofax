package org.metaborg.spoofax.core.completion;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.completion.Completion;
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
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ListImploderAttachment;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class JSGLRCompletionService implements ISpoofaxCompletionService {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRCompletionService.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon strategoCommon;
    private final IResourceService resourceService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;


    @Inject public JSGLRCompletionService(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon strategoCommon,
        IResourceService resourceService, ISpoofaxUnitService unitService, ISpoofaxSyntaxService syntaxService) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.strategoCommon = strategoCommon;
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
    }


    @Override public Iterable<ICompletion> get(int position, ISpoofaxParseUnit parseInput) throws MetaborgException {
        ISpoofaxParseUnit completionParseResult = null;

        if(!parseInput.valid()) {
            final JSGLRParserConfiguration config = new JSGLRParserConfiguration(true, true, true, 3000, position);
            final ISpoofaxInputUnit input = parseInput.input();
            final ISpoofaxInputUnit modifiedInput = unitService.inputUnit(input.source(), input.text(), input.langImpl(), input.dialect(), config);
            completionParseResult = syntaxService.parse(modifiedInput);
        }

        Collection<ICompletion> completions = Lists.newLinkedList();

        completions.addAll(completionCorrectPrograms(position, parseInput));

        if(completionParseResult != null) {
            completions.addAll(completionErroneousPrograms(completionParseResult));
        }

        return completions;

    }

    public Collection<ICompletion> completionCorrectPrograms(int position, ISpoofaxParseUnit parseResult)
        throws MetaborgException {

        Collection<ICompletion> completions = Lists.newLinkedList();

        final Iterable<IStrategoTerm> terms = tracingTermsCompletions(parseResult.ast(), new SourceRegion(position));

        final String input = parseResult.input().text();
        final FileObject location = parseResult.source();
        final ILanguageImpl language = parseResult.input().langImpl();

        final IStrategoAppl placeholder = getPlaceholder(terms);
        final Iterable<IStrategoList> lists = getLists(terms);
        final Iterable<IStrategoTerm> optionals = getOptionals(terms);

        if(placeholder != null) {
            completions.addAll(placeholderCompletions(placeholder, input, language, location));
        }
        if(Iterables.size(lists) != 0) {
            completions.addAll(listsCompletions(position, lists, input, language, location));
        }

        if(Iterables.size(optionals) != 0) {
            completions.addAll(optionalCompletions(optionals, input, language, location));
        }

        return completions;
    }


    public Collection<ICompletion> placeholderCompletions(IStrategoAppl placeholder, String input,
        ILanguageImpl language, FileObject location) throws MetaborgException {
        Collection<ICompletion> completions = Lists.newLinkedList();

        for(ILanguageComponent component : language.components()) {

            // call Stratego part of the framework to compute change
            final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location, false);
            final IStrategoTerm proposalsPlaceholder =
                strategoCommon.invoke(runtime, placeholder, "get-proposals-placeholder");

            if(proposalsPlaceholder == null) {
                logger.error("Getting proposals for {} failed", placeholder);
            }
            for(IStrategoTerm proposalTerm : proposalsPlaceholder) {
                if(!(proposalTerm instanceof IStrategoTuple)) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                if(tuple.getSubtermCount() != 3 || !(tuple.getSubterm(0) instanceof IStrategoString)
                    || !(tuple.getSubterm(1) instanceof IStrategoString)
                    || !(tuple.getSubterm(2) instanceof IStrategoAppl)) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }

                final String name = Tools.asJavaString(tuple.getSubterm(0));
                final String description = Tools.asJavaString(tuple.getSubterm(1));
                final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);

                if(change.getConstructor().getName().contains("REPLACE_TERM")) {
                    final ICompletion completion = createCompletionReplacement(name, description, change);

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


    public Collection<ICompletion> optionalCompletions(Iterable<IStrategoTerm> optionals, String input,
        ILanguageImpl language, FileObject location) throws MetaborgException {

        Collection<ICompletion> completions = Lists.newLinkedList();


        for(ILanguageComponent component : language.components()) {
            final ITermFactory termFactory = termFactoryService.get(component, false);

            for(IStrategoTerm optional : optionals) {

                ImploderAttachment attachment = optional.getAttachment(ImploderAttachment.TYPE);
                String placeholderName = attachment.getSort().substring(0, attachment.getSort().length()) + "-Plhdr";
                IStrategoAppl optionalPlaceholder =
                    termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                final IStrategoTerm strategoInput = termFactory.makeTuple(optional, optionalPlaceholder);

                // call Stratego part of the framework to compute change
                final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location, false);
                final IStrategoTerm proposalsOptional =
                    strategoCommon.invoke(runtime, strategoInput, "get-proposals-optional");

                if(proposalsOptional == null) {
                    logger.error("Getting proposals for {} failed", strategoInput);
                }

                for(IStrategoTerm proposalTerm : proposalsOptional) {
                    if(!(proposalTerm instanceof IStrategoTuple)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }
                    final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                    if(tuple.getSubtermCount() != 3 || !(tuple.getSubterm(0) instanceof IStrategoString)
                        || !(tuple.getSubterm(1) instanceof IStrategoString)
                        || !(tuple.getSubterm(2) instanceof IStrategoAppl)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    final String name = Tools.asJavaString(tuple.getSubterm(0));
                    final String description = Tools.asJavaString(tuple.getSubterm(1));
                    final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);

                    if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                        final ICompletion completion = createCompletionReplacement(name, description, change);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }

                        completions.add(completion);
                    }
                }
            }
        }
        return completions;

    }

    public Collection<ICompletion> listsCompletions(int position, Iterable<IStrategoList> lists, String input,
        ILanguageImpl language, FileObject location) throws MetaborgException {

        Collection<ICompletion> completions = Lists.newLinkedList();

        for(ILanguageComponent component : language.components()) {
            final ITermFactory termFactory = termFactoryService.get(component, false);

            for(IStrategoList list : lists) {
                ListImploderAttachment attachment = list.getAttachment(null);
                String placeholderName =
                    attachment.getSort().substring(0, attachment.getSort().length() - 1) + "-Plhdr";
                IStrategoAppl listPlaceholder = termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                final IStrategoTerm strategoInput =
                    termFactory.makeTuple(list, listPlaceholder, termFactory.makeInt(position));
                final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location, false);
                final IStrategoTerm proposalsLists =
                    strategoCommon.invoke(runtime, strategoInput, "get-proposals-list");
                if(proposalsLists == null) {
                    logger.error("Getting proposals for {} failed", strategoInput);
                    continue;
                }
                for(IStrategoTerm proposalTerm : proposalsLists) {
                    if(!(proposalTerm instanceof IStrategoTuple)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }
                    final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                    if(tuple.getSubtermCount() != 3 || !(tuple.getSubterm(0) instanceof IStrategoString)
                        || !(tuple.getSubterm(1) instanceof IStrategoString)
                        || !(tuple.getSubterm(2) instanceof IStrategoAppl)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    final String name = Tools.asJavaString(tuple.getSubterm(0));
                    final String description = Tools.asJavaString(tuple.getSubterm(1));
                    final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);


                    // if the change is inserting at the end of a list
                    if(change.getConstructor().getName().contains("INSERT_AT_END")) {

                        final ICompletion completion = createCompletionInsertAtEnd(name, description, change);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }

                        completions.add(completion);
                    } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {

                        final ICompletion completion = createCompletionInsertBefore(name, description, change);

                        if(completion == null) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }

                        completions.add(completion);
                    }

                }
            }
        }
        return completions;
    }


    private ICompletion createCompletionReplacement(String name, String description, StrategoAppl change) {

        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);

        if(change.getSubtermCount() != 2 || !(newNode instanceof IStrategoAppl) || !(oldNode instanceof IStrategoAppl)) {
            return null;
        }

        int insertionPoint, suffixPoint;

        final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);
        ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);

        // check if it's an empty node
        if(oldNodeIA.getLeftToken().getStartOffset() > oldNodeIA.getRightToken().getEndOffset()) {
            // get the last non-layout token before the new node
            int tokenPosition =
                oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(
                tokenPosition).getKind() == IToken.TK_ERROR)
                && tokenPosition > 0)
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else { // if not, do a regular replacement
            insertionPoint = oldNodeIA.getLeftToken().getStartOffset() - 1;
        }

        suffixPoint = oldNodeIA.getRightToken().getEndOffset() + 1;

        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    private ICompletion createCompletionInsertBefore(String name, String description, StrategoAppl change) {

        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);

        // expect two terms and 1st should be an element of a list
        final StrategoTerm oldList = (StrategoTerm) ParentAttachment.getParent(oldNode);

        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoAppl) || !(newNode instanceof IStrategoList)
            || !(oldList instanceof IStrategoList)) {
            return null;
        }

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);

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
            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(
                tokenPosition).getKind() == IToken.TK_ERROR)
                && tokenPosition > 0)
                tokenPosition--;

            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if inserted element is not first
            // insert after at end offset of the rightmost token of the element before the
            // completion
            StrategoTerm elementBefore = (StrategoTerm) oldList.getSubterm(indexOfElement - 1);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
        }

        // if suffix point should be the first token of the next element, next element also gets
        // pp but it should keep its indentation
        IToken checkToken = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken();
        int checkTokenIdx = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getIndex();
        suffixPoint = insertionPoint;
        for(; checkTokenIdx >= 0; checkTokenIdx--) {
            checkToken = tokenizer.getTokenAt(checkTokenIdx);
            if(tokenizer.toString(checkToken, checkToken).contains("\n")) {
                break;
            }
            suffixPoint = checkToken.getStartOffset();
        }

        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    private ICompletion createCompletionInsertAtEnd(String name, String description, StrategoAppl change) {

        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);

        // expected two lists
        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoList) || !(newNode instanceof IStrategoList)) {
            return null;
        }

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
        final ImploderAttachment oldListIA = oldNode.getAttachment(ImploderAttachment.TYPE);

        // if list is empty
        // insert after the first non-layout token before the leftmost token of the completion
        // node
        if(oldNode.getSubtermCount() == 0) {
            int tokenPosition =
                oldListIA.getLeftToken().getIndex() - 1 > 0 ? oldListIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(
                tokenPosition).getKind() == IToken.TK_ERROR)
                && tokenPosition > 0)
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if list is not empty
            // insert after at end offset of the rightmost token of the element before the
            // completion
            StrategoTerm elementBefore = (StrategoTerm) oldNode.getSubterm(oldNode.getAllSubterms().length - 1);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
        }
        suffixPoint = insertionPoint + 1;

        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    public Collection<ICompletion> completionErroneousPrograms(ISpoofaxParseUnit completionParseResult)
        throws MetaborgException {

        final FileObject location = completionParseResult.source();
        final ILanguageImpl language = completionParseResult.input().langImpl();
        final Collection<ICompletion> completions = Lists.newLinkedList();
        final Collection<IStrategoTerm> proposalsTerm = Lists.newLinkedList();

        // TODO: disambiguate the tree and break it into completion results
        Collection<IStrategoTerm> completionTerms = getCompletionTermsFromAST(completionParseResult);

        for(ILanguageComponent component : language.components()) {
            final ITermFactory termFactory = termFactoryService.get(component, false);
            for(IStrategoTerm completionTerm : completionTerms) {
                IStrategoTerm completionAst = (IStrategoTerm) completionParseResult.ast();
                final IStrategoTerm inputStratego = termFactory.makeTuple(completionAst, completionTerm);
                final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location, false);
                final IStrategoTerm proposalTerm =
                    strategoCommon.invoke(runtime, inputStratego, "get-proposals-erroneous-programs");
                if(proposalTerm == null) {
                    logger.error("Getting proposals for {} failed", inputStratego);
                }
                proposalsTerm.add(proposalTerm);
            }
            for(IStrategoTerm proposalTerm : proposalsTerm) {
                if(!(proposalTerm instanceof IStrategoTuple)) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                if(tuple.getSubtermCount() != 3 || !(tuple.getSubterm(0) instanceof IStrategoString)
                    || !(tuple.getSubterm(1) instanceof IStrategoString)
                    || !(tuple.getSubterm(2) instanceof IStrategoAppl)) {
                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                    continue;
                }
                final String name = Tools.asJavaString(tuple.getSubterm(0));
                final String description = Tools.asJavaString(tuple.getSubterm(1));
                final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);


                // if the change is inserting at the end of a list
                if(change.getConstructor().getName().contains("INSERT_AT_END")) {

                    // calls a different method because now, the program has errors that should be fixed
                    final ICompletion completion = createCompletionInsertAtEndFixing(name, description, change);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);
                } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {

                    final ICompletion completion = createCompletionInsertBeforeFixing(name, description, change);

                    if(completion == null) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    completions.add(completion);


                } else if(change.getConstructor().getName().contains("INSERTION_TERM")) {

                    final ICompletion completion = createCompletionInsertionTermFixing(name, description, change);

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


    private ICompletion createCompletionInsertionTermFixing(String name, String description, StrategoAppl change) {
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(0);

        if(change.getSubtermCount() != 1 || !(newNode instanceof IStrategoAppl)) {
            return null;
        }

        int insertionPoint, suffixPoint;

        ITokenizer tokenizer = ImploderAttachment.getTokenizer(newNode);
        final ImploderAttachment newNodeIA = newNode.getAttachment(ImploderAttachment.TYPE);

        // get the last non-layout token before the new node
        int tokenPosition = newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1 : 0;
        while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(tokenPosition)
            .getKind() == IToken.TK_ERROR) && tokenPosition > 0)
            tokenPosition--;

        insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();

        if(newNodeIA.getRightToken().getEndOffset() < newNodeIA.getRightToken().getStartOffset()) {
            // keep all the characters after the last non-layout token if completion ends with a
            // placeholder
            tokenPosition = newNodeIA.getRightToken().getIndex();
            while(tokenPosition > 0
                && (tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(tokenPosition)
                    .getStartOffset() || tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT))
                tokenPosition--;
            suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;

        } else {
            // skip all the (erroneous) characters that were in the text already
            suffixPoint = newNodeIA.getRightToken().getEndOffset() + 1;
        }
        
        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    private ICompletion createCompletionInsertBeforeFixing(String name, String description, StrategoAppl change) {

        // expect two terms and 1st should be an element of a list
        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);
        final StrategoTerm oldList = (StrategoTerm) ParentAttachment.getParent(oldNode);

        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoAppl) || !(newNode instanceof IStrategoAppl)
            || !(oldList instanceof IStrategoList)) {
            return null;
        }

        int insertionPoint, suffixPoint;

        IStrategoTerm[] subterms = ((IStrategoList) oldList).getAllSubterms();
        int indexOfCompletion;
        for(indexOfCompletion = 0; indexOfCompletion < subterms.length; indexOfCompletion++) {
            if(subterms[indexOfCompletion] == newNode)
                break;
        }
        // if inserted element is first (only two elements)
        if(indexOfCompletion == 0) {
            // insert after the first non-layout token before the leftmost token of the completion node
            ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
            final ImploderAttachment newNodeIA = newNode.getAttachment(ImploderAttachment.TYPE);
            int tokenPosition =
                newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(
                tokenPosition).getKind() == IToken.TK_ERROR)
                && tokenPosition > 0)
                tokenPosition--;

            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if inserted element is not first
            // insert after at end offset of the rightmost token of the element before the completion
            StrategoTerm elementBefore = (StrategoTerm) oldList.getSubterm(indexOfCompletion - 1);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();

        }
        // suffix point should be the first token of the next element
        suffixPoint = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getStartOffset();

        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    private ICompletion createCompletionInsertAtEndFixing(String name, String description, StrategoAppl change) {
        // expected two lists
        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);

        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoList) || !(newNode instanceof IStrategoAppl)) {
            return null;
        }

        int insertionPoint, suffixPoint;
        ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
        final ImploderAttachment newNodeIA = newNode.getAttachment(ImploderAttachment.TYPE);

        // if list is empty
        // insert after the first non-layout token before the leftmost token of the completion node
        if(oldNode.getSubtermCount() == 1) {
            int tokenPosition =
                newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1 : 0;
            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer.getTokenAt(
                tokenPosition).getKind() == IToken.TK_ERROR)
                && tokenPosition > 0)
                tokenPosition--;
            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
        } else {
            // if list is not empty
            // insert after at end offset of the rightmost token of the element before the completion
            StrategoTerm elementBefore = (StrategoTerm) oldNode.getSubterm(oldNode.getAllSubterms().length - 2);
            insertionPoint = elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
        }

        suffixPoint = insertionPoint;
        if(newNodeIA.getRightToken().getEndOffset() < newNodeIA.getRightToken().getStartOffset()) {
            // keep all the characters after the last non-layout token if completion ends with a
            // placeholder
            int tokenPosition = newNodeIA.getRightToken().getIndex();
            while(tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(tokenPosition)
                .getStartOffset()
                || tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT
                && tokenPosition > 0)
                tokenPosition--;
            suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;

        } else {
            // skip all the (erroneous) characters that were in the text already
            suffixPoint = newNodeIA.getRightToken().getEndOffset() + 1;
        }

        return new Completion(name, description, insertionPoint + 1, suffixPoint);
    }


    private Collection<IStrategoTerm> getCompletionTermsFromAST(ISpoofaxParseUnit completionParseResult) {

        StrategoAppl ast = (StrategoAppl) completionParseResult.ast();
        Collection<IStrategoTerm> completionTerm = findCompletionTerm(ast);

        return completionTerm;
    }


    private Collection<IStrategoTerm> findCompletionTerm(StrategoAppl ast) {

        final Collection<IStrategoTerm> completionTerms = Lists.newLinkedList();
        /*
         * TODO: to be implemented
         */
        
        /*final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {

        @Override public boolean visit(IStrategoTerm term) {
            ImploderAttachment ia = term.getAttachment(ImploderAttachment.TYPE);
            if(ia.isCompletion()) {
                completionTerms.add(term);
                return false;
            }
            return true;
        }
        };
        StrategoTermVisitee.bottomup(visitor, ast);
        */



        return completionTerms;
    }

    private @Nullable IStrategoAppl getPlaceholder(final Iterable<IStrategoTerm> terms) {
        for(IStrategoTerm term : terms) {
            if(term instanceof IStrategoAppl) {
                final IStrategoAppl appl = (IStrategoAppl) term;
                if(appl.getConstructor().getName().endsWith("-Plhdr")) {
                    return appl;
                }
            }
        }

        return null;
    }


    private @Nullable Iterable<IStrategoList> getLists(final Iterable<IStrategoTerm> terms) {

        Collection<IStrategoList> lists = Lists.newLinkedList();
        for(IStrategoTerm term : terms) {
            if(term instanceof IStrategoList) {
                final IStrategoList list = (IStrategoList) term;
                lists.add(list);
            } else
                break;
        }

        return lists;
    }


    private @Nullable Iterable<IStrategoTerm> getOptionals(final Iterable<IStrategoTerm> terms) {

        Collection<IStrategoTerm> optionals = Lists.newLinkedList();
        for(IStrategoTerm term : terms) {
            // check if term is nullable and it is not a list
            IToken left = ImploderAttachment.getLeftToken(term);
            IToken right = ImploderAttachment.getRightToken(term);
            if(!(term instanceof IStrategoList) && left.getStartOffset() > right.getEndOffset()) {
                optionals.add(term);
            } else
                break;
        }

        return optionals;
    }

    private Iterable<IStrategoTerm> tracingTermsCompletions(Object result, final ISourceRegion region) {
        if(result == null || region == null) {
            return Iterables2.empty();
        }
        final Collection<IStrategoTerm> parsed = Lists.newLinkedList();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                final ISourceLocation location = fromTokens(term);
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

    protected @Nullable ISourceLocation fromTokens(IStrategoTerm fragment) {
        final FileObject resource = SourceAttachment.getResource(fragment, resourceService);
        final IToken left = ImploderAttachment.getLeftToken(fragment);
        final IToken right = ImploderAttachment.getRightToken(fragment);
        ITokenizer tokenizer = ImploderAttachment.getTokenizer(fragment);
        IToken leftmostValid = left;
        IToken rightmostValid = right;
        boolean isList = (fragment instanceof IStrategoList) ? true : false;
        boolean isOptional = false;


        if(left == null || right == null) {
            return null;
        }

        if(left == right && left.getEndOffset() < left.getStartOffset()) {
            isOptional = true;
        }

        // if it's a list or a node that is empty make the element includes the surrounding layout tokens
        if(left.getStartOffset() > right.getEndOffset() || isList || isOptional) {
            for(int i = left.getIndex() - 1; i >= 0; i--) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex() + 1; i < tokenizer.getTokenCount(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }
        }

        // if not make it stripes the surrounding layout tokens
        else {
            for(int i = left.getIndex(); i < right.getIndex(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_ERROR) {
                    leftmostValid = tokenizer.getTokenAt(i + 1);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex(); i > left.getIndex(); i--) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_EOF
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_ERROR) {
                    rightmostValid = tokenizer.getTokenAt(i - 1);
                } else {
                    break;
                }
            }
        }

        final ISourceRegion region =
            JSGLRSourceRegionFactory.fromTokensLayout(leftmostValid, rightmostValid, (isOptional || isList));

        return new SourceLocation(region, resource);


    }


}
