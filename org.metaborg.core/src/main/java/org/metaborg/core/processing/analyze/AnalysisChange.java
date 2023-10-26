package org.metaborg.core.processing.analyze;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.build.UpdateKind;

public class AnalysisChange<A extends IAnalyzeUnit> {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final A result;
    @Nullable public final AnalysisException exception;


    /**
     * Creates an analysis change that represents an update to the analysis result.
     * 
     * @param resource
     *            Changed resource.
     * @param result
     *            Updated analysis result.
     * @param parentResult
     *            Parent of the updated analysis result.
     * @return Analysis change.
     */
    public static <A extends IAnalyzeUnit> AnalysisChange<A> update(FileObject resource, A result) {
        return new AnalysisChange<>(UpdateKind.Update, result.source(), result, null);
    }

    /**
     * Creates an analysis change that represents an invalidation of given resource.
     * 
     * @param resource
     *            Resource to invalidate.
     * @return Analysis change.
     */
    public static <A extends IAnalyzeUnit> AnalysisChange<A> invalidate(FileObject resource) {
        return new AnalysisChange<>(UpdateKind.Invalidate, resource, null, null);
    }

    /**
     * Creates an analysis change that represents an error that occurred while updating an analysis result.
     * 
     * @param resource
     *            Changed resource.
     * @param exception
     *            Error that occurred.
     * @return Analysis change.
     */
    public static <A extends IAnalyzeUnit> AnalysisChange<A> error(FileObject resource, AnalysisException exception) {
        return new AnalysisChange<>(UpdateKind.Error, resource, null, exception);
    }

    /**
     * Creates an analysis change that represents removal of an analysis result.
     * 
     * @param resource
     *            Resource that was removed.
     * @return Analysis change.
     */
    public static <A extends IAnalyzeUnit> AnalysisChange<A> remove(FileObject resource) {
        return new AnalysisChange<>(UpdateKind.Remove, resource, null, null);
    }


    /*
     * Use static methods to create instances.
     */
    protected AnalysisChange(UpdateKind kind, FileObject resource, @Nullable A result,
        @Nullable AnalysisException exception) {
        this.kind = kind;
        this.resource = resource;
        this.result = result;
        this.exception = exception;
    }
}
