package org.metaborg.spoofax.eclipse.processing;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;
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

/**
 * Processes analysis results and allows requesting of these analysis results.
 */
public class AnalysisResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultProcessor.class);

    private final IEclipseResourceService resourceService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;

    private final ParseResultProcessor parseResultProcessor;

    private final ConcurrentMap<FileName, BehaviorSubject<AnalysisChange>> updatesPerResource = Maps.newConcurrentMap();


    @Inject public AnalysisResultProcessor(IEclipseResourceService resourceService,
        IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService, ParseResultProcessor parseResultProcessor) {
        this.resourceService = resourceService;
        this.analysisService = analysisService;

        this.parseResultProcessor = parseResultProcessor;
    }


    /**
     * Requests the analysis result for given resource. When subscribing to the returned observer, it always returns a
     * single element; the latest analysis result, or pushes an error if an error occurred while getting it. If the
     * analysis result is cached, the observable will immediately push it. If the analysis result has been invalidated
     * (when it is in the process of being updated), it will be pushed when it has been updated. If there is no analysis
     * result yet, it will request a parse result, analyze the resource in given context, and push the analysis result.
     * 
     * The simplest way to get the analysis result is to wait for it:
     * {@code
     *   result = analysisResultProcessor.request(resource, contet, text).toBlocking().single();
     * }
     * 
     * @param resource
     *            Resource to get the analysis result for.
     * @param context
     *            Context in which the analysis should be performed, in case there is no analysis result for given
     *            resource.
     * @param text
     *            Text which should be parsed, in case there is no parse result for given resource.
     * @return Cold observable which pushes a single element when subscribed; the latest analysis result, or pushes an
     *         error if an error occurred while getting it.
     */
    public Observable<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> request(final FileObject resource,
        final IContext context, final String text) {
        return Observable.create(new OnSubscribe<AnalysisFileResult<IStrategoTerm, IStrategoTerm>>() {
            @Override public void call(Subscriber<? super AnalysisFileResult<IStrategoTerm, IStrategoTerm>> observer) {
                if(observer.isUnsubscribed()) {
                    logger.trace("Unsubscribed from analysis result request for {}", resource);
                    return;
                }

                final BehaviorSubject<AnalysisChange> updates = getUpdates(resource, context, text);
                final AnalysisChange update = updates.toBlocking().first(new Func1<AnalysisChange, Boolean>() {
                    @Override public Boolean call(AnalysisChange updateToFilter) {
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

    /**
     * Returns an observable that pushes analysis result updates to subscribers for given resource.
     * 
     * @param resource
     *            Resource to push updates for.
     * @return Hot observable that pushes updates to subscribers for given resource.
     */
    public Observable<AnalysisChange> updates(FileObject resource) {
        return getUpdates(resource.getName());
    }


    /**
     * Invalidates the analysis result for given resource. Must be followed by a call to {@link #update} or
     * {@link #error} for that resource eventually. Failing to do so will block any request made while resource was in
     * an invalid state.
     * 
     * @param resource
     *            Resource to invalidate.
     */
    public void invalidate(FileObject resource) {
        logger.trace("Invalidating analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.invalidate(resource));
    }

    /**
     * Invalidates the analysis results for sources in given parse results. Must be followed by a call to
     * {@link #update} or {@link #error} for those resources eventually. Failing to do so will block any requests made
     * while resources were in invalid states.
     * 
     * 
     * @param parseResults
     *            Parse results with sources to invalidate.
     */
    public void invalidate(Iterable<ParseResult<IStrategoTerm>> parseResults) {
        for(ParseResult<IStrategoTerm> parseResult : parseResults) {
            invalidate(parseResult.source);
        }
    }

    /**
     * Updates the analysis result for a single resource. Pushes the analysis result to subscribed requests.
     * 
     * @param result
     *            Result to update.
     * @param parentResult
     *            Parent of the result to update.
     */
    public void update(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result,
        AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult) {
        // LEGACY: analysis always returns resource on the local file system, but we expect resources in the Eclipse
        // file system here. Need to convert these, otherwise updates will not match invalidates.
        final FileObject resourceInEclipse = resourceService.rebase(result.file());
        final FileName name = resourceInEclipse.getName();

        logger.trace("Pushing analysis result for {}", name);
        final BehaviorSubject<AnalysisChange> updates = getUpdates(name);
        updates.onNext(AnalysisChange.update(resourceInEclipse, result, parentResult));
    }

    /**
     * Updates the analysis results for resources in given result. Pushes analysis results to subscribed requests.
     * 
     * @param parentResult
     *            Parent of the results to update.
     */
    public void update(AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult) {
        for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result : parentResult.fileResults) {
            update(result, parentResult);
        }
    }

    /**
     * Sets an analysis error for given resource. Pushes the analysis error to subscribed requests.
     * 
     * @param resource
     *            Resource to set an analysis error for.
     * @param exception
     *            Analysis error to set.
     */
    public void error(FileObject resource, AnalysisException exception) {
        logger.trace("Pushing analysis error for {}", resource);
        final BehaviorSubject<AnalysisChange> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.error(resource, exception));
    }


    /**
     * Sets an analysis error for sources in given parse result. Pushes analysis errors to subscribed requests.
     * 
     * @param parseResults
     *            Parse results with sources to set an analysis error for.
     * @param exception
     *            Analysis error to set.
     */
    public void error(Iterable<ParseResult<IStrategoTerm>> parseResults, AnalysisException exception) {
        for(ParseResult<IStrategoTerm> parseResult : parseResults) {
            final FileObject resource = parseResult.source;
            final BehaviorSubject<AnalysisChange> updates = getUpdates(resource.getName());
            updates.onNext(AnalysisChange.error(resource, exception));
        }
    }

    /**
     * Removes cached analysis results for given resource.
     * 
     * @param resource
     *            Resource to remove cached analysis results for.
     */
    public void remove(FileObject resource) {
        logger.trace("Removing analysis result for {}", resource);
        final BehaviorSubject<AnalysisChange> updates = getUpdates(resource.getName());
        updates.onNext(AnalysisChange.remove(resource));
    }


    private BehaviorSubject<AnalysisChange> getUpdates(FileName file) {
        final BehaviorSubject<AnalysisChange> newUpdates = BehaviorSubject.create();
        final BehaviorSubject<AnalysisChange> prevUpdates = updatesPerResource.putIfAbsent(file, newUpdates);
        return prevUpdates == null ? newUpdates : prevUpdates;
    }

    private BehaviorSubject<AnalysisChange> getUpdates(FileObject resource, IContext context, String text) {
        final FileName name = resource.getName();

        // THREADING: it is possible that two different threads asking for a subject may do the parsing twice here, as
        // this is not an atomic operation. However, the chance is very low and it does not break anything (only
        // duplicates some work), so it is acceptable.
        BehaviorSubject<AnalysisChange> updates = updatesPerResource.get(name);
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
                updates.onNext(AnalysisChange.update(resource, result, parentResult));
            } catch(AnalysisException e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.error(resource, e));
            } catch(Exception e) {
                final String message = String.format("Analysis for % failed", name);
                logger.error(message, e);
                updates.onNext(AnalysisChange.error(resource, new AnalysisException(Iterables2.singleton(resource),
                    context, message, e)));
            }
        }
        return updates;
    }
}
