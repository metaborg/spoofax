package org.metaborg.spoofax.eclipse.processing;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ParseResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParseResultProcessor.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;

    private final ConcurrentMap<FileName, BehaviorSubject<ParseUpdate>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public ParseResultProcessor(ILanguageIdentifierService languageIdentifierService,
        ISourceTextService sourceTextService, ISyntaxService<IStrategoTerm> syntaxService) {
        this.languageIdentifierService = languageIdentifierService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
    }


    public Observable<ParseResult<IStrategoTerm>> request(FileObject resource) {
        final ILanguage language = languageIdentifierService.identify(resource);
        return request(resource, language);
    }

    public Observable<ParseResult<IStrategoTerm>> request(FileObject resource, ILanguage language) {
        try {
            final String text = sourceTextService.text(resource);
            return request(resource, language, text);
        } catch(IOException e) {
            logger.error("Failed to retrieve source text for {}", resource);
            return Observable.error(e);
        }
    }

    public Observable<ParseResult<IStrategoTerm>> request(final FileObject resource, final ILanguage language,
        final String text) {
        return Observable.create(new OnSubscribe<ParseResult<IStrategoTerm>>() {
            @Override public void call(Subscriber<? super ParseResult<IStrategoTerm>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from parse result request for {}", resource);
                    return;
                }

                final BehaviorSubject<ParseUpdate> updates = getUpdates(resource, language, text);
                final ParseUpdate update = updates.toBlocking().first(new Func1<ParseUpdate, Boolean>() {
                    @Override public Boolean call(ParseUpdate updateToFilter) {
                        final UpdateKind kind = updateToFilter.kind;
                        return kind != UpdateKind.Invalidate;
                    }
                });

                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from parse result request for {}", resource);
                    return;
                }

                switch(update.kind) {
                    case Update:
                        logger.trace("Returning cached parse result for {}", resource);
                        observer.onNext(update.result);
                        observer.onCompleted();
                        break;
                    case Error:
                        logger.trace("Returning parse error for {}", resource);
                        observer.onError(update.exception);
                        break;
                    case Remove: {
                        final String message = String.format("Parse result for % was removed unexpectedly", resource);
                        logger.error(message);
                        observer.onError(new ParseException(resource, language, message));
                        break;
                    }
                    default: {
                        final String message =
                            String.format("Unexpected parse update kind % for %", update.kind, resource);
                        logger.error(message);
                        observer.onError(new ParseException(resource, language, message));
                        break;
                    }
                }
            }
        });
    }

    public Observable<ParseUpdate> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }


    public void invalidate(FileObject resource) {
        logger.trace("Invalidating parse result for {}", resource);
        final BehaviorSubject<ParseUpdate> updates = getUpdates(resource.getName());
        updates.onNext(ParseUpdate.invalidate(resource));
    }

    public void update(FileObject resource, ParseResult<IStrategoTerm> result) {
        logger.trace("Pushing parse result for {}", resource);
        final BehaviorSubject<ParseUpdate> updates = getUpdates(resource.getName());
        updates.onNext(ParseUpdate.update(result));
    }

    public void error(FileObject resource, ParseException e) {
        logger.trace("Pushing parse error for {}", resource);
        final BehaviorSubject<ParseUpdate> updates = getUpdates(resource.getName());
        updates.onNext(ParseUpdate.error(e));
    }

    public void remove(FileObject resource) {
        logger.trace("Removing parse result for {}", resource);
        final BehaviorSubject<ParseUpdate> updates = getUpdates(resource.getName());
        updates.onNext(ParseUpdate.remove(resource));
    }


    private BehaviorSubject<ParseUpdate> getUpdates(FileName file) {
        final BehaviorSubject<ParseUpdate> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<ParseUpdate> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<ParseUpdate> getUpdates(FileObject resource, ILanguage language, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything, so it is
        // acceptable.
        BehaviorSubject<ParseUpdate> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Parsing for {}", resource);
                final ParseResult<IStrategoTerm> result = syntaxService.parse(text, resource, language);
                updates.onNext(ParseUpdate.update(result));
            } catch(ParseException e) {
                final String message = String.format("Parsing for % failed", name);
                logger.error(message, e);
                updates.onNext(ParseUpdate.error(e));
            }
        }
        return updates;
    }
}
