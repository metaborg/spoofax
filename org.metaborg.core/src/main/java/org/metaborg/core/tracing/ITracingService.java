package org.metaborg.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformResult;

public interface ITracingService<P, A, T> {
    public abstract @Nullable ISourceLocation fromParsed(P fragment);

    public abstract @Nullable ISourceLocation fromAnalyzed(A fragment);

    public abstract @Nullable ISourceLocation fromTransformed(T fragment);

    public abstract @Nullable P originFromAnalyzed(A fragment);

    public abstract @Nullable P originFromTransformed(T fragment);

    public abstract Iterable<P> toParsed(ParseResult<P> result, ISourceRegion region);

    public abstract Iterable<A> toAnalyzed(AnalysisFileResult<P, A> result, ISourceRegion region);

    public abstract Iterable<T> toTransformed(TransformResult<?, T> result, ISourceRegion region);
}
