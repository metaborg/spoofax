package org.metaborg.core.processing.parse;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.UpdateKind;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ParseResultProcessor<I extends IInputUnit, P extends IParseUnit>
    implements IParseResultProcessor<I, P>, AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(ParseResultProcessor.class);

    private final ISyntaxService<I, P> syntaxService;

    private final ConcurrentMap<FileName, BehaviorSubject<ParseChange<P>>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public ParseResultProcessor(ISyntaxService<I, P> syntaxService) {
        this.syntaxService = syntaxService;
    }

    @Override public void close() {
        for(BehaviorSubject<ParseChange<P>> updates : updatesPerResource.values()) {
            updates.onComplete();
        }
        updatesPerResource.clear();
    }


    @Override public Observable<P> request(final I input) {
        final FileObject resource = input.source();
        return Observable.create(observer -> {
            if(observer.isDisposed()) {
                logger.trace("Unsubscribed from parse result request for {}", input);
                return;
            }

            final BehaviorSubject<ParseChange<P>> updates = getUpdates(input);
            final ParseChange<P> update = updates.blockingStream().filter(updateToFilter -> {
                final UpdateKind kind = updateToFilter.kind;
                return kind != UpdateKind.Invalidate;
            }).findFirst().orElse(null);
            if(update == null) {
                return;
            }

            if(observer.isDisposed()) {
                logger.trace("Unsubscribed from parse result request for {}", resource);
                return;
            }

            switch(update.kind) {
                case Update:
                    logger.trace("Returning cached parse result for {}", resource);
                    observer.onNext(update.unit);
                    observer.onComplete();
                    break;
                case Error:
                    logger.trace("Returning parse error for {}", resource);
                    observer.onError(update.exception);
                    break;
                case Remove: {
                    final String message = logger.format("Parse result for {} was removed unexpectedly", resource);
                    logger.error(message);
                    observer.onError(new ParseException(input, message));
                    break;
                }
                default: {
                    final String message =
                        logger.format("Unexpected parse update kind {} for {}", update.kind, resource);
                    logger.error(message);
                    observer.onError(new ParseException(input, message));
                    break;
                }
            }
        });
    }

    @Override public Observable<ParseChange<P>> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }

    @Override public @Nullable P get(FileObject resource) {
        final BehaviorSubject<ParseChange<P>> subject = updatesPerResource.get(resource.getName());
        if(subject == null) {
            return null;
        }
        final @Nullable ParseChange<P> change = subject.blockingStream().findFirst().orElse(null);
        if(change == null) {
            return null;
        }
        return change.unit;
    }


    @Override public void invalidate(FileObject resource) {
        logger.trace("Invalidating parse result for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>invalidate(resource));
    }

    @Override public void invalidate(ILanguageImpl impl) {
        for(BehaviorSubject<ParseChange<P>> changes : updatesPerResource.values()) {
            final @Nullable ParseChange<P> change = changes.blockingStream().findFirst().orElse(null);
            if(change != null && change.unit != null && impl.equals(change.unit.input().langImpl())) {
                changes.onNext(ParseChange.<P>invalidate(change.resource));
            }
        }
    }

    @Override public void update(FileObject resource, P unit) {
        logger.trace("Pushing parse result for {}", resource);
        final BehaviorSubject<ParseChange<P>> updates = getUpdates(resource.getName());
        updates.onNext(ParseChange.<P>update(unit));
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

    private BehaviorSubject<ParseChange<P>> getUpdates(I unit) {
        final FileObject resource = unit.source();
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
                final P result = syntaxService.parse(unit);
                updates.onNext(ParseChange.update(result));
            } catch(ParseException e) {
                final String message = String.format("Parsing for %s failed", name);
                logger.error(message, e);
                updates.onNext(ParseChange.<P>error(e));
            }
        }
        return updates;
    }
}
