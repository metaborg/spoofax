package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.source.ISourceLocation;
import org.metaborg.spoofax.core.source.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.TransformResult;

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
