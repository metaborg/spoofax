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
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceLocation;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.syntax.SourceAttachment;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
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
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class CompletionService implements ICompletionService<IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(CompletionService.class);

    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon strategoCommon;
    private final IResourceService resourceService;


    @Inject public CompletionService(ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon strategoCommon, IResourceService resourceService) {
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.strategoCommon = strategoCommon;
        this.resourceService = resourceService;
    }


    @Override public Iterable<ICompletion> get(ParseResult<IStrategoTerm> parseResult, int position)
        throws MetaborgException {
        if(parseResult.result == null) {
            return Iterables2.empty();
        }

        final FileObject location = parseResult.source;
        final ILanguageImpl language = parseResult.language;
        final Collection<ICompletion> completions = Lists.newLinkedList();
        final Iterable<IStrategoTerm> terms = tracingTermsCompletions(parseResult.result, new SourceRegion(position));
        final IStrategoAppl placeholder = getPlaceholder(terms);
        final Iterable<IStrategoList> lists = getLists(terms);

        if(placeholder != null) {


            for(ILanguageComponent component : language.components()) {
                final ITermFactory termFactory = termFactoryService.get(component);
                final IStrategoTerm input =
                    termFactory.makeTuple(placeholder, parseResult.result, termFactory.makeInt(position));
                final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                final IStrategoTerm proposalsTerm = strategoCommon.invoke(runtime, input, "get-proposals");
                if(proposalsTerm == null) {
                    logger.error("Getting proposals for {} failed", input);
                }
                for(IStrategoTerm proposalTerm : proposalsTerm) {
                    if(!(proposalTerm instanceof IStrategoTuple)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }
                    final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                    if(tuple.getSubtermCount() != 2 || !(tuple.getSubterm(0) instanceof IStrategoString)
                        || !(tuple.getSubterm(1) instanceof IStrategoString)) {
                        logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                        continue;
                    }

                    final String description = Tools.asJavaString(tuple.getSubterm(0));
                    final String text = Tools.asJavaString(tuple.getSubterm(1));
                    ImploderAttachment placeholderAttachment = placeholder.getAttachment(null);
                    
                    final int leftOffset = placeholderAttachment.getLeftToken().getStartOffset();
                    final int rightOffset = placeholderAttachment.getRightToken().getEndOffset();
                    
                    //final String fullText = parseResult.input.substring(0, leftOffset) + text + parseResult.input.substring(rightOffset+1);
                    
                    final Collection<ICompletionItem> items = createItemsFromString(text);

                    final Completion completion = new Completion(items, description);
                    completions.add(completion);
                }
            }

            return completions;
        }

        if(lists != null) {


            for(ILanguageComponent component : language.components()) {
                final ITermFactory termFactory = termFactoryService.get(component);
                
                for(IStrategoList list : lists) {
                    ListImploderAttachment attachment = list.getAttachment(null);
                    String placeholderName = attachment.getSort().substring(0, attachment.getSort().length() - 1) + "-Plhdr";
                    IStrategoAppl listPlaceholder =
                        termFactory.makeAppl(termFactory.makeConstructor(placeholderName, 0));
                    final IStrategoTerm input =
                        termFactory.makeTuple(list, listPlaceholder, parseResult.result, termFactory.makeInt(position));
                    final HybridInterpreter runtime = strategoRuntimeService.runtime(component, location);
                    final IStrategoTerm proposalsTerm = strategoCommon.invoke(runtime, input, "get-proposals-list");
                    if(proposalsTerm == null) {
                        logger.error("Getting proposals for {} failed", input);
                    }
                    for(IStrategoTerm proposalTerm : proposalsTerm) {
                        if(!(proposalTerm instanceof IStrategoTuple)) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }
                        final IStrategoTuple tuple = (IStrategoTuple) proposalTerm;
                        if(tuple.getSubtermCount() != 2 || !(tuple.getSubterm(0) instanceof IStrategoString)
                            || !(tuple.getSubterm(1) instanceof IStrategoString)) {
                            logger.error("Unexpected proposal term {}, skipping", proposalTerm);
                            continue;
                        }

                        final String description = Tools.asJavaString(tuple.getSubterm(0));
                        final String text = Tools.asJavaString(tuple.getSubterm(1));
                        //final String fullText = parseResult.input.substring(0, position) + text + parseResult.input.substring(position);
                        final Collection<ICompletionItem> items = createItemsFromString(text);
                        
                        final Completion completion = new Completion(items, description);
                        completions.add(completion);
                    }
                }
            }


            return completions;


        }


        return Iterables2.empty();

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

    private Iterable<IStrategoTerm> tracingTermsCompletions(IStrategoTerm ast, final ISourceRegion region) {
        if(ast == null || region == null) {
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
        StrategoTermVisitee.bottomup(visitor, ast);
        return parsed;
    }


    protected @Nullable ISourceLocation fromTokens(IStrategoTerm fragment) {
        final FileObject resource = SourceAttachment.getResource(fragment, resourceService);
        final IToken left = ImploderAttachment.getLeftToken(fragment);
        final IToken right = ImploderAttachment.getRightToken(fragment);
        IToken leftmostIncludingLayout = left;
        IToken rightmostIncludingLayout = right;


        if(left == null || right == null) {
            return null;
        }
        if(fragment instanceof StrategoList) {
            ITokenizer tokenizer = ImploderAttachment.getTokenizer(fragment);

            for(int i = left.getIndex() - 1; i >= 0; i--) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT) {
                    leftmostIncludingLayout = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }

            for(int i = right.getIndex() + 1; i < tokenizer.getTokenCount(); i++) {
                if(tokenizer.getTokenAt(i).getKind() == IToken.TK_LAYOUT
                    || tokenizer.getTokenAt(i).getKind() == IToken.TK_EOF) {
                    rightmostIncludingLayout = tokenizer.getTokenAt(i);
                } else {
                    break;
                }
            }
        }
        final ISourceRegion region =
            JSGLRSourceRegionFactory.fromTokensLayout(leftmostIncludingLayout, rightmostIncludingLayout);

        return new SourceLocation(region, resource);


    }


}
