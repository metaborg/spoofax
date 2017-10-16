package org.metaborg.core.processing.analyze;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeResult;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.UpdateKind;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.processing.parse.IParseResultRequester;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class AnalysisResultProcessor<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate>
    implements IAnalysisResultProcessor<I, P, A> {
    private static final ILogger logger = LoggerUtils.logger(AnalysisResultProcessor.class);

    private final IAnalysisService<P, A, AU> analysisService;
    private final IParseResultRequester<I, P> parseResultRequester;

    private final ConcurrentMap<FileName, BehaviorSubject<AnalysisChange<A>>> updatesPerResource =
        Maps.newConcurrentMap();


    @Inject public AnalysisResultProcessor(IAnalysisService<P, A, AU> analysisService,
        IParseResultRequester<I, P> parseResultRequester) {
        this.analysisService = analysisService;
        this.parseResultRequester = parseResultRequester;
    }


    @Override public Observable<A> request(final I input, final IContext context) {
        if(input.detached()) {
            throw new MetaborgRuntimeException("Cannot request updates for detached (no source) units");
        }
        final FileObject resource = input.source();
        return Observable.create(new OnSubscribe<A>() {
            @Override public void call(Subscriber<? super A> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from analysis result request for {}", resource);
                    return;
                }

                final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(input, context);
                final AnalysisChange<A> update = updates.toBlocking().first(new Func1<AnalysisChange<A>, Boolean>() {
                    @Override public Boolean call(AnalysisChange<A> updateToFilter) {
                        final UpdateKind kind = updateToFilter.kind;
                        return kind != UpdateKind.Invalidate;
                    }
                });

                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from analysis result request for {}", resource);
                    return;
                }

                switch(update.kind) {
                    case Update:
                        logger.trace("Returning cached analysis result for {}", resource);
                        observer.onNext(update.result);
                        observer.onCompleted();
                        break;
                    case Error:
                        logger.trace("Returning analysis error for {}", resource);
                        observer.onError(update.exception);
                        break;
                    case Remove: {
                        final String message =
                            String.format("Analysis result for %s was removed unexpectedly", resource);
                        logger.error(message);
                        observer.onError(new AnalysisException(context, message));
                        break;
                    }
                    default: {
                        final String message =
                            String.format("Unexpected analysis update kind %s for %s", update.kind, resource);
                        logger.error(message);
                        observer.onError(new MetaborgRuntimeException(message));
                        break;
                    }
                }
            }
        });
    }

    @Override public Observable<AnalysisChange<A>> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }

    @Override public @Nullable A get(FileObject resource) {
        final BehaviorSubject<AnalysisChange<A>> subject = updatesPerResource.get(resource.getName());
        if(subject == null) {
            return null;
        }
        final AnalysisChange<A> change = subject.toBlocking().firstOrDefault(null);
        if(change == null) {
            return null;
        }
        return change.result;
    }


    @Override public void invalidate(FileObject resource) {
        logger.trace("Invalidating analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<A>invalidate(resource));
    }

    @Override public void invalidate(Iterable<P> results) {
        for(P parseResult : results) {
            if(parseResult.detached()) {
                throw new MetaborgRuntimeException("Cannot invalidate results for detached (no source) units");
            }
            invalidate(parseResult.source());
        }
    }

    @Override public void invalidate(ILanguageImpl impl) {
        final Iterator<BehaviorSubject<AnalysisChange<A>>> it = updatesPerResource.values().iterator();
        while(it.hasNext()) {
            final BehaviorSubject<AnalysisChange<A>> changes = it.next();
            final AnalysisChange<A> change = changes.toBlocking().firstOrDefault(null);
            if(change != null && change.result != null && impl.equals(change.result.context().language())) {
                changes.onNext(AnalysisChange.<A>invalidate(change.resource));
            }
        }
    }

    @Override public void update(A result, Set<FileName> removedResources) {
        if(result.detached()) {
            throw new MetaborgRuntimeException("Cannot process updates for detached (no source) units");
        }
        final FileObject resource = result.source();
        final FileName name = resource.getName();
        if(removedResources.contains(name)) {
            remove(resource);
        } else {
            logger.trace("Pushing analysis result for {}", name);
            final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(name);
            updates.onNext(AnalysisChange.update(resource, result));
        }
    }

    @Override public void error(FileObject resource, AnalysisException exception) {
        logger.trace("Pushing analysis error for {}", resource);
        final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<A>error(resource, exception));
    }

    @Override public void error(Iterable<P> results, AnalysisException exception) {
        for(P parseResult : results) {
            final FileObject resource = parseResult.source();
            if(parseResult.detached()) {
                throw new MetaborgRuntimeException("Cannot process analysis errors for detached (no source) units");
            }
            final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(resource.getName());
            updates.onNext(AnalysisChange.<A>error(resource, exception));
        }
    }

    @Override public void remove(FileObject resource) {
        logger.trace("Removing analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange<A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<A>remove(resource));
    }


    private BehaviorSubject<AnalysisChange<A>> getUpdates(FileName file) {
        final BehaviorSubject<AnalysisChange<A>> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<AnalysisChange<A>> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<AnalysisChange<A>> getUpdates(I input, IContext context) {
        if(input.detached()) {
            throw new MetaborgRuntimeException("Cannot get updates for detached (no source) units");
        }
        final FileObject source = input.source();
        final FileName name = source.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything (only
        // duplicates some work), so it is acceptable.
        BehaviorSubject<AnalysisChange<A>> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Requesting parse result for {}", source);
                final P parseResult = parseResultRequester.request(input).toBlocking().single();
                if(!parseResult.valid()) {
                    updates.onNext(AnalysisChange.<A>error(source, new AnalysisException(context, "Parsing failed")));
                    return updates;
                }

                logger.trace("Analysing for {}", source);
                final IAnalyzeResult<A, AU> result;
                try(IClosableLock lock = context.write()) {
                    result = analysisService.analyze(parseResult, context);
                }

                updates.onNext(AnalysisChange.<A>update(source, result.result()));
                // HACK: ignore analyze unit updates from result.updates(), may cause incrementality problems.
            } catch(AnalysisException e) {
                final String message = logger.format("Analysis for {} failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.<A>error(source, e));
            } catch(Exception e) {
                final String message = logger.format("Analysis for {} failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.<A>error(source, new AnalysisException(context, message, e)));
            }
        }
        return updates;
    }
}
