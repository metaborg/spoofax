package org.metaborg.spoofax.core.processing.parse;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.processing.UpdateKind;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import com.google.common.collect.Maps;
import com.google.inject.Inject;


public class ParseResultProcessor<P> implements IParseResultProcessor<P> {
    private static final Logger logger = LoggerFactory.getLogger(ParseResultProcessor.class);

    private final ISyntaxService<P> syntaxService;

    private final ConcurrentMap<FileName, BehaviorSubject<ParseChange<P>>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public ParseResultProcessor(ISyntaxService<P> syntaxService) {
        this.syntaxService = syntaxService;
    }


    @Override public Observable<ParseResult<P>> request(final FileObject resource, final ILanguage language,
        final String text) {
        return Observable.create(new OnSubscribe<ParseResult<P>>() {
            @Override public void call(Subscriber<? super ParseResult<P>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from parse result request for {}", resource);
                    return;
                }

                final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource, language, text);
                final ParseChange<P> update = updates.toBlocking().first(new Func1<ParseChange<P>, Boolean>() {
                    @Override public Boolean call(ParseChange<P> updateToFilter) {
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

    @Override public Observable<ParseChange<P>> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }

    @Override public @Nullable ParseResult<P> get(FileObject resource) {
        final BehaviorSubject<ParseChange<P>> subject = updatesPerResource.get(resource.getName());
        if(subject == null) {
            return null;
        }
        final ParseChange<P> change = subject.toBlocking().firstOrDefault(null);
        if(change == null) {
            return null;
        }
        return change.result;
    }


    @Override public void invalidate(FileObject resource) {
        logger.trace("Invalidating parse result for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>invalidate(resource));
    }

    @Override public void update(FileObject resource, ParseResult<P> result) {
        logger.trace("Pushing parse result for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>update(result));
    }

    @Override public void error(FileObject resource, ParseException exception) {
        logger.trace("Pushing parse error for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>error(exception));
    }

    @Override public void remove(FileObject resource) {
        logger.trace("Removing parse result for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>remove(resource));
    }


    private BehaviorSubject<ParseChange<P>> getUpdates(FileName file) {
        final BehaviorSubject<ParseChange<P>> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<ParseChange<P>> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<ParseChange<P>> getUpdates(FileObject resource, ILanguage language, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything (only
        // duplicates some work), so it is acceptable.
        BehaviorSubject<ParseChange<P>> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Parsing for {}", resource);
                final ParseResult<P> result = syntaxService.parse(text, resource, language, null);
                updates.onNext(ParseChange.update(result));
            } catch(ParseException e) {
                final String message = String.format("Parsing for % failed", name);
                logger.error(message, e);
                updates.onNext(ParseChange.<P>error(e));
            }
        }
        return updates;
    }
}
