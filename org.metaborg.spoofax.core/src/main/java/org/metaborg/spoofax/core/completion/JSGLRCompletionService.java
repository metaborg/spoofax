package org.metaborg.spoofax.core.completion;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.completion.Completion;
import org.metaborg.core.completion.ICompletion;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.jsglr.client.CompletionStateSet;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.State;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class JSGLRCompletionService implements ICompletionService {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRCompletionService.class);

    private final ISyntaxService<?> syntaxService;


    @Inject public JSGLRCompletionService(ISyntaxService<?> syntaxService) {
        this.syntaxService = syntaxService;
    }


    @Override public Iterable<ICompletion> get(ParseResult<?> parseResult, int position) throws MetaborgException {
        final ILanguage language = parseResult.language;
        final CompletionFacet facet = language.facet(CompletionFacet.class);
        if(facet == null) {
            final String message =
                String.format("Cannot get completions of %s, it does not have a completion facet", language);
            logger.error(message);
            throw new MetaborgException(message);
        }
        final String input = parseResult.input;

        final ParseResult<?> completionParseResult;
        try {
            final IParserConfiguration config = new JSGLRParserConfiguration(true, false, true, 2000, position);
            completionParseResult = syntaxService.parse(input, parseResult.source, language, config);
        } catch(ParseException e) {
            final String message = "Cannot get completions, parsinged failed unexpectedly";
            logger.error(message, e);
            throw e;
        }

        final SGLRParseResult sglrParseResult = (SGLRParseResult) completionParseResult.parserSpecificData;
        final CompletionStateSet completionStates = sglrParseResult.completionStates;

        final State lastState = completionStates.last();
        final int stateId = lastState.stateNumber;
        final Collection<ICompletion> completions = Lists.newLinkedList();
        final Iterable<CompletionDefinition> completionDefinitions = facet.get(stateId);

        for(CompletionDefinition completionDefinition : completionDefinitions) {
            completions.add(new Completion(completionDefinition.items, completionDefinition.description));

            for(State state : completionStates.states()) {
                if(!state.equals(lastState)) {
                    final Iterable<CompletionDefinition> enclosingCompletions = facet.get(state.stateNumber);
                    for(CompletionDefinition enclosingCompletionDefinition : enclosingCompletions) {
                        if(enclosingCompletionDefinition.expectedSort.equals(completionDefinition.producedSort)) {
                            completions.add(new Completion(mixedCompletion(completionDefinition.items,
                                enclosingCompletionDefinition.items), enclosingCompletionDefinition.description));
                        }
                    }
                }

            }
        }
        return completions;
    }

    /**
     * Concatenates completions from a state and its enclosing states
     * @param items completion items from current state
     * @param items2 completion items from enclosing state 
     * @return
     */
    private Iterable<ICompletionItem>
        mixedCompletion(Iterable<ICompletionItem> items, Iterable<ICompletionItem> items2) {

        Collection<ICompletionItem> resultingItems = Lists.newLinkedList();

        for(ICompletionItem firstItems : items) {
            resultingItems.add(firstItems);
        }
        
        boolean first = true;

        for(ICompletionItem remainingItems : items2) {
            if(first) {
                first = false;
                continue;
            }
            resultingItems.add(remainingItems);
        }

        return resultingItems;
    }
}
