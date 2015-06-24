package org.metaborg.spoofax.core.tracing;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;

public interface IHoverService<P, A> {
    public abstract @Nullable Hover hover(int offset, AnalysisFileResult<P, A> result) throws SpoofaxException;
}
