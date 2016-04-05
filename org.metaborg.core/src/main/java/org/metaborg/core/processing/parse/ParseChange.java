package org.metaborg.core.processing.parse;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.UpdateKind;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ParseException;

public class ParseChange<P extends IParseUnit> {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final P unit;
    @Nullable public final ParseException exception;


    /**
     * Creates a parse change that represents an update to the parse result.
     *
     * @param result
     *            Updated parse result.
     * @return Parse change.
     */
    public static <P extends IParseUnit> ParseChange<P> update(P unit) {
        return new ParseChange<>(UpdateKind.Update, unit.source(), unit, null);
    }

    /**
     * Creates a parse change that represents an invalidation of given resource.
     * 
     * @param resource
     *            Resource to invalidate.
     * @return Parse change.
     */
    public static <P extends IParseUnit> ParseChange<P> invalidate(FileObject resource) {
        return new ParseChange<>(UpdateKind.Invalidate, resource, null, null);
    }

    /**
     * Creates a parse change that represents an error that occurred while updating a parse result.
     *
     * @param exception
     *            Error that occurred.
     * @return Parse change.
     */
    public static <P extends IParseUnit> ParseChange<P> error(ParseException exception) {
        return new ParseChange<>(UpdateKind.Error, exception.unit.source(), null, exception);
    }

    /**
     * Creates a parse change that represents removal of a parse result.
     * 
     * @param resource
     *            Resource that was removed.
     * @return Parse change.
     */
    public static <P extends IParseUnit> ParseChange<P> remove(FileObject resource) {
        return new ParseChange<>(UpdateKind.Remove, resource, null, null);
    }


    /*
     * Use static methods to create instances.
     */
    protected ParseChange(UpdateKind kind, FileObject resource, @Nullable P unit, @Nullable ParseException exception) {
        this.kind = kind;
        this.resource = resource;
        this.unit = unit;
        this.exception = exception;
    }
}
