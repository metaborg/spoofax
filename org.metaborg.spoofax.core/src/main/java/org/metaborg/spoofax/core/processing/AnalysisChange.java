package org.metaborg.spoofax.core.processing;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;

public class AnalysisChange<P, A> {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final AnalysisFileResult<P, A> result;
    @Nullable public final AnalysisResult<P, A> parentResult;
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
    public static <P, A> AnalysisChange<P, A> update(FileObject resource, AnalysisFileResult<P, A> result,
        AnalysisResult<P, A> parentResult) {
        return new AnalysisChange<P, A>(UpdateKind.Update, result.source, result, parentResult, null);
    }

    /**
     * Creates an analysis change that represents an invalidation of given resource.
     * 
     * @param resource
     *            Resource to invalidate.
     * @return Analysis change.
     */
    public static <P, A> AnalysisChange<P, A> invalidate(FileObject resource) {
        return new AnalysisChange<P, A>(UpdateKind.Invalidate, resource, null, null, null);
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
    public static <P, A> AnalysisChange<P, A> error(FileObject resource, AnalysisException exception) {
        return new AnalysisChange<P, A>(UpdateKind.Error, resource, null, null, exception);
    }

    /**
     * Creates an analysis change that represents removal of an analysis result.
     * 
     * @param resource
     *            Resource that was removed.
     * @return Analysis change.
     */
    public static <P, A> AnalysisChange<P, A> remove(FileObject resource) {
        return new AnalysisChange<P, A>(UpdateKind.Remove, resource, null, null, null);
    }


    /*
     * Use static methods to create instances.
     */
    protected AnalysisChange(UpdateKind kind, FileObject resource, AnalysisFileResult<P, A> result,
        AnalysisResult<P, A> parentResult, AnalysisException exception) {
        this.kind = kind;
        this.resource = resource;
        this.result = result;
        this.parentResult = parentResult;
        this.exception = exception;
    }
}
