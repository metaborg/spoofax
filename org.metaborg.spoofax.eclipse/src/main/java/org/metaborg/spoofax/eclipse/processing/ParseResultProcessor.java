package org.metaborg.spoofax.eclipse.processing;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.util.Nullable;
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

/**
 * Processes parse results and allows requesting of these parse results.
 */
public class ParseResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParseResultProcessor.class);

    private final ISyntaxService<IStrategoTerm> syntaxService;

    private final ConcurrentMap<FileName, BehaviorSubject<ParseChange>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public ParseResultProcessor(ISyntaxService<IStrategoTerm> syntaxService) {
        this.syntaxService = syntaxService;
    }


    /**
     * Requests the parse result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest parse result, or pushes an error if an error occurred while getting it. If the parse
     * result is cached, the observable will immediately push it. If the parse result has been invalidated (when it is
     * in the process of being updated), it will be pushed when it has been updated. If there is no parse result yet, it
     * will parse with given text and push the parse result.
     * 
     * The simplest way to get the parse result is to wait for it:
     * {@code
     *   result = parseResultProcessor.request(resource, language, text).toBlocking().single();
     * }
     * 
     * @param resource
     *            Resource to get the parse result for.
     * @param language
     *            Language given text should be parsed with, in case there is no parse result for given resource.
     * @param text
     *            Text which should be parsed, in case there is no parse result for given resource.
     * @return Cold observable which pushes a single element when subscribed; the latest parse result, or pushes an
     *         error if an error occurred while getting it.
     */
    public Observable<ParseResult<IStrategoTerm>> request(final FileObject resource, final ILanguage language,
        final String text) {
        return Observable.create(new OnSubscribe<ParseResult<IStrategoTerm>>() {
            @Override public void call(Subscriber<? super ParseResult<IStrategoTerm>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from parse result request for {}", resource);
                    return;
                }

                final BehaviorSubject<ParseChange> updates = getUpdates(resource, language, text);
                final ParseChange update = updates.toBlocking().first(new Func1<ParseChange, Boolean>() {
                    @Override public Boolean call(ParseChange updateToFilter) {
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

    /**
     * Returns an observable that pushes parse result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    public Observable<ParseChange> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }

    /**
     * @return Latest parse result for given resource, or null if there is none.
     */
    public @Nullable ParseResult<IStrategoTerm> get(FileObject resource) {
        final BehaviorSubject<ParseChange> subject = updatesPerResource.get(resource.getName());
        if(subject == null) {
            return null;
        }
        final ParseChange change = subject.toBlocking().firstOrDefault(null);
        if(change == null) {
            return null;
        }
        return change.result;
    }


    /**
     * Invalidates the parse result for given resource. Must be followed by a call to {@link #update} or {@link #error}
     * for that resource eventually. Failing to do so will block any request made while resource was in an invalid
     * state.
     * 
     * @param resource
     *            Resource to invalidate.
     */
    public void invalidate(FileObject resource) {
        logger.trace("Invalidating parse result for {}", resource);
        final BehaviorSubject<ParseChange> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.invalidate(resource));
    }

    /**
     * Updates the parse result for a single resource. Pushes the parse result to subscribed requests.
     * 
     * @param result
     *            Result to update.
     * @param parentResult
     *            Parent of the result to update.
     */
    public void update(FileObject resource, ParseResult<IStrategoTerm> result) {
        logger.trace("Pushing parse result for {}", resource);
        final BehaviorSubject<ParseChange> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.update(result));
    }

    /**
     * Sets a parse error for given resource. Pushes the parse error to subscribed requests.
     * 
     * @param resource
     *            Resource to set a parse error for.
     * @param exception
     *            Parse error to set.
     */
    public void error(FileObject resource, ParseException exception) {
        logger.trace("Pushing parse error for {}", resource);
        final BehaviorSubject<ParseChange> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.error(exception));
    }

    /**
     * Removes cached parse results for given resource.
     * 
     * @param resource
     *            Resource to remove cached parse results for.
     */
    public void remove(FileObject resource) {
        logger.trace("Removing parse result for {}", resource);
        final BehaviorSubject<ParseChange> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.remove(resource));
    }


    private BehaviorSubject<ParseChange> getUpdates(FileName file) {
        final BehaviorSubject<ParseChange> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<ParseChange> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }


    private BehaviorSubject<ParseChange> getUpdates(FileObject resource, ILanguage language, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything (only
        // duplicates some work), so it is acceptable.
        BehaviorSubject<ParseChange> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Parsing for {}", resource);
                final ParseResult<IStrategoTerm> result = syntaxService.parse(text, resource, language);
                updates.onNext(ParseChange.update(result));
            } catch(ParseException e) {
                final String message = String.format("Parsing for % failed", name);
                logger.error(message, e);
                updates.onNext(ParseChange.error(e));
            }
        }
        return updates;
    }
}
