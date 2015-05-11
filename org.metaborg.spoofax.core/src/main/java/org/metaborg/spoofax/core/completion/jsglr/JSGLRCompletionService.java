package org.metaborg.spoofax.core.completion.jsglr;

import java.util.Set;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.completion.Completion;
import org.metaborg.spoofax.core.completion.ICompletion;
import org.metaborg.spoofax.core.completion.ICompletionItem;
import org.metaborg.spoofax.core.completion.ICompletionService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.State;

import com.google.inject.Inject;

public class JSGLRCompletionService implements ICompletionService {
    private static final Logger logger = LoggerFactory.getLogger(JSGLRCompletionService.class);

    private final ISyntaxService<?> syntaxService;


    @Inject public JSGLRCompletionService(ISyntaxService<?> syntaxService) {
        this.syntaxService = syntaxService;
    }


    @Override public Iterable<ICompletion> get(ParseResult<?> parseResult, int position) throws SpoofaxException {
        final ILanguage language = parseResult.language;
        final CompletionFacet facet = language.facet(CompletionFacet.class);
        if(facet == null) {
            final String message =
                String.format("Cannot get completions of %s, it does not have a completion facet", language);
            logger.error(message);
            throw new SpoofaxException(message);
        }
        final String input = parseResult.input.substring(0, position);

        final ParseResult<?> completionParseResult;
        try {
            // TODO: set completion mode + cursor position
            completionParseResult = syntaxService.parse(input, parseResult.source, language);
        } catch(ParseException e) {
            final String message = "Cannot get completions, parsinged failed unexpectedly";
            logger.error(message, e);
            throw e;
        }

        if(completionParseResult.parserSpecificData == null) {
            return Iterables2.<ICompletion>empty();
        }

        final SGLRParseResult sglrParseResult = (SGLRParseResult) completionParseResult.parserSpecificData;
        final Set<State> completionStates = sglrParseResult.completionStates;
        // TODO: finish this when eduardo completes item sets.

        return Iterables2.from(new ICompletion[] {
            new Completion(Iterables2.from(new ICompletionItem[] { new TextCompletionItem("This is a"),
                new TextCompletionItem(" completion!") })),
            new Completion(Iterables2.from(new ICompletionItem[] { new TextCompletionItem("if "),
                new PlaceholderCompletionItem("Exp", "condition"), new TextCompletionItem(" then\n  "),
                new PlaceholderCompletionItem("Exp", "condition"), new TextCompletionItem("\n  "),
                new CursorCompletionItem(), new TextCompletionItem("\nfi") })),
            new Completion(Iterables2.from(new ICompletionItem[] { new PlaceholderCompletionItem("Exp", "left"),
                new TextCompletionItem(" + "), new PlaceholderCompletionItem("Exp", "right") })), });
    }
}
