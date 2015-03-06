package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ParseChange {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final ParseResult<IStrategoTerm> result;
    @Nullable public final ParseException exception;


    /**
     * Creates a parse change that represents an update to the parse result.
     * 
     * @param resource
     *            Changed resource.
     * @param result
     *            Updated parse result.
     * @param parentResult
     *            Parent of the updated parse result.
     * @return Parse change.
     */
    public static ParseChange update(ParseResult<IStrategoTerm> result) {
        return new ParseChange(UpdateKind.Update, result.source, result, null);
    }

    /**
     * Creates a parse change that represents an invalidation of given resource.
     * 
     * @param resource
     *            Resource to invalidate.
     * @return Parse change.
     */
    public static ParseChange invalidate(FileObject resource) {
        return new ParseChange(UpdateKind.Invalidate, resource, null, null);
    }

    /**
     * Creates a parse change that represents an error that occurred while updating a parse result.
     * 
     * @param resource
     *            Changed resource.
     * @param exception
     *            Error that occurred.
     * @return Parse change.
     */
    public static ParseChange error(ParseException exception) {
        return new ParseChange(UpdateKind.Error, exception.resource, null, exception);
    }

    /**
     * Creates a parse change that represents removal of a parse result.
     * 
     * @param resource
     *            Resource that was removed.
     * @return Parse change.
     */
    public static ParseChange remove(FileObject resource) {
        return new ParseChange(UpdateKind.Remove, resource, null, null);
    }


    /*
     * Use static methods to create instances.
     */
    protected ParseChange(UpdateKind kind, FileObject resource, ParseResult<IStrategoTerm> result,
        ParseException exception) {
        this.kind = kind;
        this.resource = resource;
        this.result = result;
        this.exception = exception;
    }
}
