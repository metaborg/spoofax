package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AnalysisUpdate {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final AnalysisFileResult<IStrategoTerm, IStrategoTerm> result;
    @Nullable public final AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult;
    @Nullable public final AnalysisException exception;


    public static AnalysisUpdate update(FileObject resource, AnalysisFileResult<IStrategoTerm, IStrategoTerm> result,
        AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult) {
        return new AnalysisUpdate(UpdateKind.Update, result.file(), result, parentResult, null);
    }

    public static AnalysisUpdate invalidate(FileObject resource) {
        return new AnalysisUpdate(UpdateKind.Invalidate, resource, null, null, null);
    }

    public static AnalysisUpdate error(FileObject resource, AnalysisException exception) {
        return new AnalysisUpdate(UpdateKind.Error, resource, null, null, exception);
    }

    public static AnalysisUpdate remove(FileObject resource) {
        return new AnalysisUpdate(UpdateKind.Remove, resource, null, null, null);
    }


    protected AnalysisUpdate(UpdateKind kind, FileObject resource,
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> result,
        AnalysisResult<IStrategoTerm, IStrategoTerm> parentResult, AnalysisException exception) {
        this.kind = kind;
        this.resource = resource;
        this.result = result;
        this.parentResult = parentResult;
        this.exception = exception;
    }
}
