package org.metaborg.spoofax.core.completion;

import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.completion.Completion;
import org.metaborg.core.completion.ICompletion;
import org.metaborg.core.completion.ICompletionItem;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceLocation;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.syntax.SourceAttachment;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
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
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;
import org.strategoxt.HybridInterpreter;

import ch.qos.logback.core.subst.Tokenizer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class JSGLRCompletionService implements ICompletionService {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRCompletionService.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon strategoCommon;
    private final IResourceService resourceService;
    private final ISyntaxService<?> syntaxService;


    @Inject public JSGLRCompletionService(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon strategoCommon,
        IResourceService resourceService, ISyntaxService<?> syntaxService) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.strategoCommon = strategoCommon;
        this.resourceService = resourceService;
        this.syntaxService = syntaxService;
    }


    @Override public Iterable<ICompletion> get(ParseResult<?> parseResult, int position) throws MetaborgException {

        final ParseResult<?> completionParseResult;
        final IParserConfiguration config = new JSGLRParserConfiguration(true, true, true, 300000000, position);
        completionParseResult =
            syntaxService.parse(parseResult.input, parseResult.source, parseResult.language, config);

        // should disambiguate the tree and break it into completion results
        Collection<IStrategoTerm> completionTerms = getCompletionTermsFromAST(completionParseResult);


        // present the result as suggestions/final result

        final FileObject location = parseResult.source;
        final ILanguageImpl language = parseResult.language;
        final Collection<ICompletion> completions = Lists.newLinkedList();
        final Collection<IStrategoTerm> proposalsTerm = Lists.newLinkedList();


        if(!completionTerms.isEmpty()) {
            for(ILanguageComponent component : language.components()) {
                final ITermFactory termFactory = termFactoryService.get(component);
                for(IStrategoTerm completionTerm : completionTerms) {
                    IStrategoTerm completionAst = (IStrategoTerm) completionParseResult.result;
                    final IStrategoTerm input = termFactory.makeTuple(completionAst, completionTerm);
                    final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                    final IStrategoTerm proposalTerm =
                        strategoCommon.invoke(runtime, input, "get-proposals-erroneous-programs");
                    if(proposalTerm == null) {
                        logger.error("Getting proposals for {} failed", input);
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
                    String text = null;
                    int insertionPoint = position;
                    int suffixPoint = position;
                    final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);


                    // if the change is inserting at the end of a list
                    if(change.getConstructor().getName().contains("INSERT_AT_END")) {
                        // expected two lists
                        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
                        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);
                        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoList)
                            || !(newNode instanceof IStrategoAppl)) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }
                        ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
                        final ImploderAttachment newNodeIA = newNode.getAttachment(ImploderAttachment.TYPE);

                        // if list is empty
                        // insert after the first non-layout token before the leftmost token of the completion node
                        if(((IStrategoList) oldNode).getSubtermCount() == 1) {
                            int tokenPosition =
                                newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1
                                    : 0;
                            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                                .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                                tokenPosition--;
                            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
                        } else {
                            // if list is not empty
                            // insert after at end offset of the rightmost token of the element before the completion
                            StrategoTerm elementBefore =
                                (StrategoTerm) oldNode.getSubterm(oldNode.getAllSubterms().length - 2);
                            insertionPoint =
                                elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();
                        }

                        suffixPoint = insertionPoint;
                        if(newNodeIA.getRightToken().getEndOffset() < newNodeIA.getRightToken().getStartOffset()) {
                            // keep all the characters after the last non-layout token if completion ends with a
                            // placeholder
                            int tokenPosition = newNodeIA.getRightToken().getIndex();
                            while(tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(
                                tokenPosition).getStartOffset()
                                || tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT
                                && tokenPosition > 0)
                                tokenPosition--;
                            suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;

                        } else
                            // skip all the (erroneous) characters that were in the text already
                            suffixPoint = newNodeIA.getRightToken().getEndOffset() + 1;

                        text = parseResult.input.substring(0, insertionPoint + 1);
                        text += description;
                        text += parseResult.input.substring(suffixPoint);
                    } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {
                        // expect two terms and 1st should be an element of a list
                        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
                        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);
                        final StrategoTerm oldList = (StrategoTerm) ParentAttachment.getParent(oldNode);
                        if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoAppl)
                            || !(newNode instanceof IStrategoAppl) || !(oldList instanceof IStrategoList)) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }
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
                                newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1
                                    : 0;
                            while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                                .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                                tokenPosition--;

                            insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
                        } else {
                            // if inserted element is not first
                            // insert after at end offset of the rightmost token of the element before the completion
                            StrategoTerm elementBefore = (StrategoTerm) oldList.getSubterm(indexOfCompletion - 1);
                            insertionPoint =
                                elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken().getEndOffset();

                        }
                        // suffix point should be the first token of the next element
                        suffixPoint = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getStartOffset();
                        text = parseResult.input.substring(0, insertionPoint + 1);
                        text += description;
                        text += parseResult.input.substring(suffixPoint);

                    } else if(change.getConstructor().getName().contains("INSERTION_TERM")) {
                        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(0);
                        if(change.getSubtermCount() != 1 || !(newNode instanceof IStrategoAppl)) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }

                        ITokenizer tokenizer = ImploderAttachment.getTokenizer(newNode);
                        final ImploderAttachment newNodeIA = newNode.getAttachment(ImploderAttachment.TYPE);

                        // get the last non-layout token before the new node
                        int tokenPosition =
                            newNodeIA.getLeftToken().getIndex() - 1 > 0 ? newNodeIA.getLeftToken().getIndex() - 1 : 0;
                        while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                            .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                            tokenPosition--;

                        insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();

                        if(newNodeIA.getRightToken().getEndOffset() < newNodeIA.getRightToken().getStartOffset()) {
                            // keep all the characters after the last non-layout token if completion ends with a
                            // placeholder
                            tokenPosition = newNodeIA.getRightToken().getIndex();
                            while(tokenPosition > 0
                                && (tokenizer.getTokenAt(tokenPosition).getEndOffset() < tokenizer.getTokenAt(
                                    tokenPosition).getStartOffset() || tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT))
                                tokenPosition--;
                            suffixPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset() + 1;

                        } else
                            // skip all the (erroneous) characters that were in the text already
                            suffixPoint = newNodeIA.getRightToken().getEndOffset() + 1;

                        text = parseResult.input.substring(0, insertionPoint + 1);
                        text += description;
                        text += parseResult.input.substring(suffixPoint);
                    }
                    final Collection<ICompletionItem> items = createItemsFromString(text);

                    final Completion completion = new Completion(items, name);
                    completions.add(completion);
                }
            }

            return completions;

        } else {

            final Iterable<IStrategoTerm> terms =
                tracingTermsCompletions(parseResult.result, new SourceRegion(position));
            final IStrategoAppl placeholder = getPlaceholder(terms);
            final Iterable<IStrategoList> lists = getLists(terms);
            final Iterable<IStrategoTerm> optionals = getOptionals(terms);

            if(placeholder != null) {
                for(ILanguageComponent component : language.components()) {
                    final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
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
                        String text = null;
                        final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);
                        final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
                        final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);
                        int insertionPoint = position;
                        int suffixPoint = position;

                        if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                            if(change.getSubtermCount() != 2 || !(newNode instanceof IStrategoAppl)
                                || !(oldNode instanceof IStrategoAppl)) {
                                logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                                continue;
                            }

                            ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
                            final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);

                            // get the last non-layout token before the new node

                            insertionPoint = oldNodeIA.getLeftToken().getStartOffset() - 1;

                            // skip all the (erroneous) characters that were in the text already
                            suffixPoint = oldNodeIA.getRightToken().getEndOffset() + 1;


                            text = parseResult.input.substring(0, insertionPoint + 1);
                            text += description;
                            text += parseResult.input.substring(suffixPoint);
                        }
                        final Collection<ICompletionItem> items = createItemsFromString(text);

                        final Completion completion = new Completion(items, name);
                        completions.add(completion);
                    }
                }

                return completions;
            }

            if(Iterables.size(lists) != 0) {
                for(ILanguageComponent component : language.components()) {
                    final ITermFactory termFactory = termFactoryService.get(component);

                    for(IStrategoList list : lists) {
                        ListImploderAttachment attachment = list.getAttachment(null);
                        String placeholderName =
                            attachment.getSort().substring(0, attachment.getSort().length() - 1) + "-Plhdr";
                        IStrategoAppl listPlaceholder =
                            termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                        final IStrategoTerm input =
                            termFactory.makeTuple(list, listPlaceholder, termFactory.makeInt(position));
                        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                        final IStrategoTerm proposalsLists =
                            strategoCommon.invoke(runtime, input, "get-proposals-list");
                        if(proposalsLists == null) {
                            logger.error("Getting proposals for {} failed", input);
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
                            String text = null;
                            final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);
                            final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
                            final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);

                            // if the change is inserting at the end of a list
                            if(change.getConstructor().getName().contains("INSERT_AT_END")) {
                                // expected two lists
                                if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoList)
                                    || !(newNode instanceof IStrategoList)) {
                                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                                    continue;
                                }

                                ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
                                final ImploderAttachment oldListIA = oldNode.getAttachment(ImploderAttachment.TYPE);
                                int insertionPoint = position;

                                // if list is empty
                                // insert after the first non-layout token before the leftmost token of the completion
                                // node
                                if(((IStrategoList) oldNode).getSubtermCount() == 0) {
                                    int tokenPosition =
                                        oldListIA.getLeftToken().getIndex() - 1 > 0 ? oldListIA.getLeftToken()
                                            .getIndex() - 1 : 0;
                                    while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                                        .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                                        tokenPosition--;
                                    insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
                                } else {
                                    // if list is not empty
                                    // insert after at end offset of the rightmost token of the element before the
                                    // completion
                                    StrategoTerm elementBefore =
                                        (StrategoTerm) oldNode.getSubterm(oldNode.getAllSubterms().length - 1);
                                    insertionPoint =
                                        elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken()
                                            .getEndOffset();
                                }
                                int suffixPoint = insertionPoint + 1;

                                text = parseResult.input.substring(0, insertionPoint + 1);
                                text += description;
                                text += parseResult.input.substring(suffixPoint);
                            } else if(change.getConstructor().getName().contains("INSERT_BEFORE")) {
                                // expect two terms and 1st should be an element of a list
                                final StrategoTerm oldList = (StrategoTerm) ParentAttachment.getParent(oldNode);
                                ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
                                if(change.getSubtermCount() != 2 || !(oldNode instanceof IStrategoAppl)
                                    || !(newNode instanceof IStrategoList) || !(oldList instanceof IStrategoList)) {
                                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                                    continue;
                                }

                                int insertionPoint = position;
                                IStrategoTerm[] subterms = ((IStrategoList) oldList).getAllSubterms();
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
                                        oldNodeIA.getLeftToken().getIndex() - 1 > 0 ? oldNodeIA.getLeftToken()
                                            .getIndex() - 1 : 0;
                                    while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                                        .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                                        tokenPosition--;

                                    insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();
                                } else {
                                    // if inserted element is not first
                                    // insert after at end offset of the rightmost token of the element before the
                                    // completion
                                    StrategoTerm elementBefore = (StrategoTerm) oldList.getSubterm(indexOfElement - 1);
                                    insertionPoint =
                                        elementBefore.getAttachment(ImploderAttachment.TYPE).getRightToken()
                                            .getEndOffset();

                                }

                                // if suffix point should be the first token of the next element, next element also gets
                                // pp but it should keep its indentation
                                final IStrategoTerm indentOldNode =
                                    strategoCommon.invoke(runtime, oldNode, "get-indent");
                                IToken checkToken = oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken();
                                int checkTokenIdx =
                                    oldNode.getAttachment(ImploderAttachment.TYPE).getLeftToken().getIndex();
                                int suffixPoint = insertionPoint;
                                for(; checkTokenIdx >= 0; checkTokenIdx--) {
                                    checkToken = tokenizer.getTokenAt(checkTokenIdx);
                                    if(tokenizer.toString(checkToken, checkToken).contains("\n")) {
                                        break;
                                    }                                    
                                    suffixPoint = checkToken.getStartOffset();
                                }

                                text = parseResult.input.substring(0, insertionPoint + 1);
                                text += description;
                                text += parseResult.input.substring(suffixPoint);
                            }

                            final Collection<ICompletionItem> items = createItemsFromString(text);

                            final Completion completion = new Completion(items, name);
                            completions.add(completion);
                        }
                    }
                }
            }

            if(Iterables.size(optionals) != 0) {
                for(ILanguageComponent component : language.components()) {
                    final ITermFactory termFactory = termFactoryService.get(component);

                    for(IStrategoTerm optional : optionals) {
                        ImploderAttachment attachment = optional.getAttachment(ImploderAttachment.TYPE);
                        String placeholderName =
                            attachment.getSort().substring(0, attachment.getSort().length()) + "-Plhdr";
                        IStrategoAppl optionalPlaceholder =
                            termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                        final IStrategoTerm input = termFactory.makeTuple(optional, optionalPlaceholder);
                        final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                        final IStrategoTerm proposalsOptional =
                            strategoCommon.invoke(runtime, input, "get-proposals-optional");
                        if(proposalsOptional == null) {
                            logger.error("Getting proposals for {} failed", input);
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
                            String text = null;
                            final StrategoAppl change = (StrategoAppl) tuple.getSubterm(2);
                            final StrategoTerm oldNode = (StrategoTerm) change.getSubterm(0);
                            final StrategoTerm newNode = (StrategoTerm) change.getSubterm(1);
                            int insertionPoint = position;
                            int suffixPoint = position;

                            if(change.getConstructor().getName().contains("REPLACE_TERM")) {

                                if(change.getSubtermCount() != 2 || !(newNode instanceof IStrategoAppl)
                                    || !(oldNode instanceof IStrategoAppl)) {
                                    logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                                    continue;
                                }

                                ITokenizer tokenizer = ImploderAttachment.getTokenizer(oldNode);
                                final ImploderAttachment oldNodeIA = oldNode.getAttachment(ImploderAttachment.TYPE);

                                // get the last non-layout token before the new node
                                int tokenPosition =
                                    oldNodeIA.getLeftToken().getIndex() - 1 > 0
                                        ? oldNodeIA.getLeftToken().getIndex() - 1 : 0;
                                while((tokenizer.getTokenAt(tokenPosition).getKind() == IToken.TK_LAYOUT || tokenizer
                                    .getTokenAt(tokenPosition).getKind() == IToken.TK_ERROR) && tokenPosition > 0)
                                    tokenPosition--;

                                insertionPoint = tokenizer.getTokenAt(tokenPosition).getEndOffset();

                                // skip all the (erroneous) characters that were in the text already
                                suffixPoint = oldNodeIA.getRightToken().getEndOffset() + 1;

                                text = parseResult.input.substring(0, insertionPoint + 1);
                                text += description;
                                text += parseResult.input.substring(suffixPoint);
                            }
                            final Collection<ICompletionItem> items = createItemsFromString(text);

                            final Completion completion = new Completion(items, name);
                            completions.add(completion);
                        }

                    }
                }

            }

            return completions;

        }

    }

    private Collection<IStrategoTerm> getCompletionTermsFromAST(ParseResult<?> completionParseResult) {

        StrategoAppl ast = (StrategoAppl) completionParseResult.result;

        Collection<IStrategoTerm> completionTerm = findCompletionTerm(ast);


        return completionTerm;
    }


    private Collection<IStrategoTerm> findCompletionTerm(StrategoAppl ast) {

        final Collection<IStrategoTerm> completionTerms = Lists.newLinkedList();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
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



        return completionTerms;
    }


    private Collection<ICompletionItem> createItemsFromString(String input) {

        StringBuffer sb = new StringBuffer();
        Collection<ICompletionItem> result = new LinkedList<ICompletionItem>();

        for(int i = 0; i < input.length(); i++) {

            if(input.charAt(i) == '[') {
                i++;
                if(input.charAt(i) == '[') { // might have found placeholder
                    i++;

                    while(i < input.length() && input.charAt(i) == '[') { // nested brackets
                        sb.append(input.charAt(i));
                        i++;
                    }

                    StringBuffer placeholderName = new StringBuffer();
                    while(i < input.length() && input.charAt(i) != ']') {
                        String charAti = String.valueOf(input.charAt(i));

                        if(!charAti.matches("[a-zA-Z_]")) { // not placeholder: abort
                            break;
                        }

                        placeholderName.append(charAti);
                        i++;

                    }

                    if(i >= input.length() || input.charAt(i) != ']') { // add two [[ and placeholder name to buffer
                        sb.append("[[" + placeholderName);
                        placeholderName.setLength(0);
                        continue;
                    }

                    i++;

                    final TextCompletionItem item = new TextCompletionItem(sb.toString());
                    result.add(item);

                    final PlaceholderCompletionItem placeholder =
                        new PlaceholderCompletionItem(placeholderName.toString(), placeholderName.toString());
                    result.add(placeholder);

                    sb.setLength(0);
                    continue;
                }
                sb.append(input.charAt(i - 1));
                sb.append(input.charAt(i));
            } else {
                sb.append(input.charAt(i));
            }
        }

        if(sb.length() != 0) {
            final TextCompletionItem item = new TextCompletionItem(sb.toString());
            result.add(item);
        }

        return result;
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
