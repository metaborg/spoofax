package org.metaborg.core.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;


public abstract class SyntaxService<I extends IInputUnit, P extends IParseUnit> implements ISyntaxService<I, P> {
    private static final ILogger logger = LoggerUtils.logger(SyntaxService.class);

    protected final Map<String, ? extends IParser<I, P>> parsers;


    @jakarta.inject.Inject public SyntaxService(Map<String, ? extends IParser<I, P>> parsers) {
        this.parsers = parsers;
    }


    @Override public boolean available(ILanguageImpl langImpl) {
        return parser(langImpl) != null;
    }


    @Override public P parse(I input, IProgress progress, ICancel cancel) throws ParseException, InterruptedException {
        final ILanguageImpl langImpl = input.langImpl();
        final IParser<I, P> parser = parser(langImpl);
        if(parser == null) {
            final String message = logger.format("Cannot get a parser for {}", langImpl);
            throw new ParseException(input, message);
        }
        return parser.parse(input, progress, cancel);
    }

    @Override public Collection<P> parseAll(Iterable<I> inputs, IProgress progress, ICancel cancel)
        throws ParseException, InterruptedException {
        final Collection<P> results = new ArrayList<>();
        for(I input : inputs) {
            results.add(parse(input, progress, cancel));
        }
        return results;
    }


    private IParser<I, P> parser(ILanguageImpl language) {
        final ParseFacet facet = language.facet(ParseFacet.class);
        if(facet == null) {
            return null;
        }
        return parsers.get(facet.type);
    }
}
