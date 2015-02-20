package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ParseUpdate {
    public final UpdateKind kind;
    public final FileObject resource;
    @Nullable public final ParseResult<IStrategoTerm> result;
    @Nullable public final ParseException exception;


    public static ParseUpdate update(ParseResult<IStrategoTerm> result) {
        return new ParseUpdate(UpdateKind.Update, result.source, result, null);
    }

    public static ParseUpdate invalidate(FileObject resource) {
        return new ParseUpdate(UpdateKind.Invalidate, resource, null, null);
    }

    public static ParseUpdate error(ParseException exception) {
        return new ParseUpdate(UpdateKind.Error, exception.resource, null, exception);
    }

    public static ParseUpdate remove(FileObject resource) {
        return new ParseUpdate(UpdateKind.Remove, resource, null, null);
    }


    protected ParseUpdate(UpdateKind kind, FileObject resource, ParseResult<IStrategoTerm> result,
        ParseException exception) {
        this.kind = kind;
        this.resource = resource;
        this.result = result;
        this.exception = exception;
    }
}
