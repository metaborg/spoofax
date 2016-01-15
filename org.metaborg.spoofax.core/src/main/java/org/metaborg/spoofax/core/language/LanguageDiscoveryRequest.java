package org.metaborg.spoofax.core.language;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Iterables;

@Deprecated
public class LanguageDiscoveryRequest implements ILanguageDiscoveryRequest {
    public final boolean available;
    public final FileObject location;
    public final Iterable<String> errors;
    public final Iterable<Throwable> exceptions;
    public final @Nullable IStrategoAppl esvTerm;
    public final @Nullable IProjectSettings settings;
    public final @Nullable SyntaxFacet syntaxFacet;
    public final @Nullable StrategoRuntimeFacet strategoRuntimeFacet;


    public LanguageDiscoveryRequest(boolean available, FileObject location, Iterable<String> errors,
        Iterable<Throwable> exceptions, @Nullable IStrategoAppl esvTerm, @Nullable IProjectSettings settings,
        @Nullable SyntaxFacet syntaxFacet, @Nullable StrategoRuntimeFacet strategoRuntimeFacet) {
        this.available = available;
        this.location = location;
        this.errors = errors;
        this.exceptions = exceptions;
        this.esvTerm = esvTerm;
        this.settings = settings;
        this.syntaxFacet = syntaxFacet;
        this.strategoRuntimeFacet = strategoRuntimeFacet;
    }

    public LanguageDiscoveryRequest(FileObject location, IStrategoAppl esvTerm, IProjectSettings settings,
        @Nullable SyntaxFacet syntaxFacet, @Nullable StrategoRuntimeFacet strategoRuntimeFacet) {
        this(true, location, Iterables2.<String>empty(), Iterables2.<Throwable>empty(), esvTerm, settings, syntaxFacet,
            strategoRuntimeFacet);
    }

    public LanguageDiscoveryRequest(FileObject location, Iterable<String> errors, Iterable<Throwable> exceptions) {
        this(false, location, errors, exceptions, null, null, null, null);
    }

    public LanguageDiscoveryRequest(FileObject location, Iterable<String> errors) {
        this(false, location, errors, Iterables2.<Throwable>empty(), null, null, null, null);
    }

    public LanguageDiscoveryRequest(FileObject location, String error) {
        this(false, location, Iterables2.singleton(error), Iterables2.<Throwable>empty(), null, null, null, null);
    }

    public LanguageDiscoveryRequest(FileObject location, Throwable exception) {
        this(false, location, Iterables2.<String>empty(), Iterables2.singleton(exception), null, null, null, null);
    }


    @Override public boolean available() {
        return available;
    }

    @Override public FileObject location() {
        return location;
    }

    @Override public Iterable<String> errors() {
        return errors;
    }

    @Override public Iterable<Throwable> exceptions() {
        return exceptions;
    }

    @Override public @Nullable String errorSummary() {
        if(available) {
            return null;
        }
        final boolean hasErrors = !Iterables.isEmpty(errors);
        final boolean hasExceptions = !Iterables.isEmpty(exceptions);
        if(!hasErrors && !hasExceptions) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Cannot create language component at ");
        sb.append(location);
        sb.append(".\n");
        if(hasErrors) {
            sb.append("The following errors occured: \n");
            for(String error : errors) {
                sb.append("  ");
                sb.append(error);
                sb.append('\n');
            }
        }

        if(hasExceptions) {
            if(hasErrors) {
                sb.append('\n');
            }
            sb.append("The following exceptions occured: \n");
            for(Throwable exception : exceptions) {
                sb.append("  ");
                sb.append(ExceptionUtils.getStackTrace(exception));
                sb.append('\n');
            }
        }

        return sb.toString();
    }
}
