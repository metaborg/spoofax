package org.metaborg.spoofax.core.processing.analyze;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.processing.UpdateKind;
import org.metaborg.spoofax.core.processing.parse.IParseResultRequester;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AnalysisResultProcessor<P, A> implements IAnalysisResultProcessor<P, A> {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultProcessor.class);

    // private final IResourceService resourceService;
    private final IAnalysisService<P, A> analysisService;

    private final IParseResultRequester<P> parseResultProcessor;

    private final ConcurrentMap<FileName, BehaviorSubject<AnalysisChange<P, A>>> updatesPerResource = Maps
        .newConcurrentMap();


    @Inject public AnalysisResultProcessor(IAnalysisService<P, A> analysisService,
        IParseResultRequester<P> parseResultProcessor) {
        this.analysisService = analysisService;

        this.parseResultProcessor = parseResultProcessor;
    }


    @Override public Observable<AnalysisFileResult<P, A>> request(final FileObject resource, final IContext context,
        final String text) {
        return Observable.create(new OnSubscribe<AnalysisFileResult<P, A>>() {
            @Override public void call(Subscriber<? super AnalysisFileResult<P, A>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from analysis result request for {}", resource);
                    return;
                }

                final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(resource, context, text);
                final AnalysisChange<P, A> update =
                    updates.toBlocking().first(new Func1<AnalysisChange<P, A>, Boolean>() {
                        @Override public Boolean call(AnalysisChange<P, A> updateToFilter) {
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
                            String.format("Analysis result for % was removed unexpectedly", resource);
                        logger.error(message);
                        observer.onError(new AnalysisException(Iterables2.singleton(resource), context, message));
                        break;
                    }
                    default: {
                        final String message =
                            String.format("Unexpected analysis update kind % for %", update.kind, resource);
                        logger.error(message);
                        observer.onError(new SpoofaxRuntimeException(message));
                        break;
                    }
                }
            }
        });
    }

    @Override public Observable<AnalysisChange<P, A>> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }


    @Override public void invalidate(FileObject resource) {
        logger.trace("Invalidating analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<P, A>invalidate(resource));
    }

    @Override public void invalidate(Iterable<ParseResult<P>> parseResults) {
        for(ParseResult<P> parseResult : parseResults) {
            invalidate(parseResult.source);
        }
    }

    @Override public void update(AnalysisFileResult<P, A> result, AnalysisResult<P, A> parentResult,
        Set<FileName> removedResources) {
        // LEGACY: analysis always returns resources on the local file system, but we expect resources in the Eclipse
        // file system here. Need to rebase resources on the local file system to the Eclipse file system, otherwise
        // updates will not match invalidates.
        // final FileObject resource = resourceService.rebase(result.source);
        // GTODO: enable this behavior again, disabled because it is Eclipse-dependent.
        final FileObject resource = result.source;
        final FileName name = resource.getName();
        if(removedResources.contains(name)) {
            remove(resource);
        } else {
            logger.trace("Pushing analysis result for {}", name);
            final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(name);
            updates.onNext(AnalysisChange.<P, A>update(resource, result, parentResult));
        }
    }

    @Override public void update(AnalysisResult<P, A> parentResult, Set<FileName> removedResources) {
        for(AnalysisFileResult<P, A> result : parentResult.fileResults) {
            update(result, parentResult, removedResources);
        }
    }

    @Override public void update(AnalysisResult<P, A> parentResult) {
        update(parentResult, Sets.<FileName>newHashSet());
    }

    @Override public void error(FileObject resource, AnalysisException exception) {
        logger.trace("Pushing analysis error for {}", resource);
        final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<P, A>error(resource, exception));
    }

    @Override public void error(Iterable<ParseResult<P>> parseResults, AnalysisException exception) {
        for(ParseResult<P> parseResult : parseResults) {
            final FileObject resource = parseResult.source;
            final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(resource.getName());
            updates.onNext(AnalysisChange.<P, A>error(resource, exception));
        }
    }

    @Override public void remove(FileObject resource) {
        logger.trace("Removing analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange<P, A>> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.<P, A>remove(resource));
    }


    private BehaviorSubject<AnalysisChange<P, A>> getUpdates(FileName file) {
        final BehaviorSubject<AnalysisChange<P, A>> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<AnalysisChange<P, A>> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<AnalysisChange<P, A>> getUpdates(FileObject resource, IContext context, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything (only
        // duplicates some work), so it is acceptable.
        BehaviorSubject<AnalysisChange<P, A>> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Requesting parse result for {}", resource);
                final ParseResult<P> parseResult =
                    parseResultProcessor.request(resource, context.language(), text).toBlocking().single();

                logger.trace("Analysing for {}", resource);
                final AnalysisResult<P, A> parentResult =
                    analysisService.analyze(Iterables2.singleton(parseResult), context);
                final AnalysisFileResult<P, A> result = Iterables.get(parentResult.fileResults, 0);
                updates.onNext(AnalysisChange.<P, A>update(resource, result, parentResult));
            } catch(AnalysisException e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.<P, A>error(resource, e));
            } catch(Exception e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.<P, A>error(resource,
                    new AnalysisException(Iterables2.singleton(resource), context, message, e)));
            }
        }
        return updates;
    }
}
