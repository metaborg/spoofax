package org.metaborg.spoofax.eclipse.processing;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class AnalysisResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultProcessor.class);

    private final IEclipseResourceService resourceService;
    private final ISourceTextService sourceTextService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;

    private final ParseResultProcessor parseResultProcessor;

    private final ConcurrentMap<FileName, BehaviorSubject<AnalysisUpdate>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public AnalysisResultProcessor(IEclipseResourceService resourceService,
        ISourceTextService sourceTextService, IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ParseResultProcessor parseResultProcessor) {
        this.resourceService = resourceService;
        this.sourceTextService = sourceTextService;
        this.analysisService = analysisService;

        this.parseResultProcessor = parseResultProcessor;
    }


    public Observable<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> request(FileObject resource, IContext context) {
        try {
            final String text = sourceTextService.text(resource);
            return request(resource, context, text);
        } catch(IOException e) {
            logger.error("Failed to retrieve source text for {}", resource);
            return Observable.error(e);
        }
    }

    public Observable<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> request(final FileObject resource,
        final IContext context, final String text) {
        return Observable.create(new OnSubscribe<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>() {
            @Override public void call(Subscriber<? super AnalysisFileResult<IStrategoTerm, IStrategoTerm>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from analysis result request for {}", resource);
                    return;
                }

                final BehaviorSubject<AnalysisUpdate> updates = getUpdates(resource, context, text);
                final AnalysisUpdate update = updates.toBlocking().first(new Func1<AnalysisUpdate, Boolean>() {
                    @Override public Boolean call(AnalysisUpdate updateToFilter) {
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
                        observer.onError(new AnalysisException(Iterables2.singleton(resource), context, message));
                        break;
                    }
                }
            }
        });
    }

    public Observable<AnalysisUpdate> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }


    public void invalidate(FileObject resource) {
        logger.trace("Invalidating analysis result for {}", resource);
        final BehaviorSubject<AnalysisUpdate> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisUpdate.invalidate(resource));
    }

    public void invalidate(Iterable<FileObject> resources) {
        for(FileObject resource : resources) {
            invalidate(resource);
        }
    }

    public void update(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result,
        AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult) {
        // LEGACY: analysis always returns resource on the local file system, but we expect resources in the Eclipse
        // file system here. Need to convert these, otherwise updates will not match invalidates.
        final FileObject resourceInEclipse = resourceService.rebase(result.file());
        final FileName name = resourceInEclipse.getName();

        logger.trace("Pushing analysis result for {}", name);
        final BehaviorSubject<AnalysisUpdate> updates = getUpdates(name);
        updates.onNext(AnalysisUpdate.update(resourceInEclipse, result, parentResult));
    }

    public void update(AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult) {
        for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result : parentResult.fileResults) {
            update(result, parentResult);
        }
    }

    public void error(FileObject resource, AnalysisException e) {
        logger.trace("Pushing analysis error for {}", resource);
        final BehaviorSubject<AnalysisUpdate> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisUpdate.error(resource, e));
    }

    public void error(Iterable<FileObject> resources, AnalysisException e) {
        for(FileObject resource : resources) {
            final BehaviorSubject<AnalysisUpdate> updates = getUpdates(resource.getName());
            updates.onNext(AnalysisUpdate.error(resource, e));
        }
    }

    public void remove(FileObject resource) {
        logger.trace("Removing analysis result for {}", resource);
        final BehaviorSubject<AnalysisUpdate> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisUpdate.remove(resource));
    }


    private BehaviorSubject<AnalysisUpdate> getUpdates(FileName file) {
        final BehaviorSubject<AnalysisUpdate> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<AnalysisUpdate> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<AnalysisUpdate> getUpdates(FileObject resource, IContext context, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything, so it is
        // acceptable.
        BehaviorSubject<AnalysisUpdate> updates = updatesPerResource.get(name);
        if(updates == null) {
            updates = BehaviorSubject.create();
            updatesPerResource.put(name, updates);
            try {
                logger.trace("Requesting parse result for {}", resource);
                final ParseResult<IStrategoTerm> parseResult =
                    parseResultProcessor.request(resource, context.language(), text).toBlocking().single();

                logger.trace("Analysing for {}", resource);
                final AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult =
                    analysisService.analyze(Iterables2.singleton(parseResult), context);
                final AnalysisFileResult<IStrategoTerm, IStrategoTerm> result =
                    Iterables.get(parentResult.fileResults, 0);
                updates.onNext(AnalysisUpdate.update(resource, result, parentResult));
            } catch(AnalysisException e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisUpdate.error(resource, e));
            } catch(Exception e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisUpdate.error(resource, new AnalysisException(Iterables2.singleton(resource),
                    context, message, e)));
            }
        }
        return updates;
    }
}
