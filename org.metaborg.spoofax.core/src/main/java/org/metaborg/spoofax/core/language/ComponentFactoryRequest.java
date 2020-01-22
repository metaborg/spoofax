package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.language.IComponentCreationConfigRequest;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Iterables;

public class ComponentFactoryRequest implements IComponentCreationConfigRequest {
    private final boolean available;
    private final FileObject location;
    private final Collection<String> errors;
    private final Collection<Throwable> exceptions;
    @Nullable private final ILanguageComponentConfig config;
    @Nullable private final IStrategoAppl esvTerm;
    @Nullable private final SyntaxFacet syntaxFacet;
    @Nullable private final StrategoRuntimeFacet strategoRuntimeFacet;


    /**
     * Initializes a new instance for a successful request.
     *
     * @param location
     *            The location of the language component.
     * @param config
     *            The configuration of the language component.
     * @param esvTerm
     *            The ESV term.
     * @param syntaxFacet
     *            The syntax facet.
     * @param strategoRuntimeFacet
     *            The Stratego runtime facet.
     */
    public ComponentFactoryRequest(FileObject location, @Nullable ILanguageComponentConfig config,
        @Nullable IStrategoAppl esvTerm, @Nullable SyntaxFacet syntaxFacet,
        @Nullable StrategoRuntimeFacet strategoRuntimeFacet) {
        this.available = true;
        this.location = location;
        this.errors = Collections.emptyList();
        this.exceptions = Collections.emptyList();
        this.esvTerm = esvTerm;
        this.config = config;
        this.syntaxFacet = syntaxFacet;
        this.strategoRuntimeFacet = strategoRuntimeFacet;
    }

    /**
     * Initializes a new instance for a failed request.
     *
     * @param location
     *            The location of the language component.
     * @param errors
     *            The error messages that were raised during the request.
     * @param exceptions
     *            The exceptions that were raised during the request.
     */
    public ComponentFactoryRequest(FileObject location, Collection<String> errors, Collection<Throwable> exceptions) {
        this.available = false;
        this.location = location;
        this.errors = errors != null ? errors : Collections.<String>emptyList();
        this.exceptions = exceptions != null ? exceptions : Collections.<Throwable>emptyList();
        this.esvTerm = null;
        this.config = null;
        this.syntaxFacet = null;
        this.strategoRuntimeFacet = null;
    }

    /**
     * Initializes a new instance for a failed request.
     *
     * @param location
     *            The location of the language component.
     * @param errors
     *            The error messages that were raised during the request.
     */
    public ComponentFactoryRequest(FileObject location, Collection<String> errors) {
        this(location, errors, null);
    }

    /**
     * Initializes a new instance for a failed request.
     *
     * @param location
     *            The location of the language component.
     * @param error
     *            The error message that was raised during the request.
     */
    public ComponentFactoryRequest(FileObject location, String error) {
        this(location, Collections.singletonList(error), null);
    }

    /**
     * Initializes a new instance for a failed request.
     *
     * @param location
     *            The location of the language component.
     * @param exception
     *            The exception that was raised during the request.
     */
    public ComponentFactoryRequest(FileObject location, Throwable exception) {
        this(location, null, Collections.singletonList(exception));
    }


    /**
     * {@inheritDoc}
     */
    @Override public boolean valid() {
        return this.available;
    }

    /**
     * {@inheritDoc}
     */
    @Override public FileObject location() {
        return this.location;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable public ILanguageComponentConfig config() {
        return this.config;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection<String> errors() {
        return this.errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Collection<Throwable> exceptions() {
        return this.exceptions;
    }

    /**
     * Gets the ESV term.
     *
     * @return The ESV term; or <code>null</code> when there is no ESV term.
     */
    public @Nullable IStrategoAppl esvTerm() {
        return this.esvTerm;
    }

    /**
     * Gets the syntax facet.
     *
     * @return The syntax facet; or <code>null</code> when there is no Syntax facet.
     */
    public @Nullable SyntaxFacet syntaxFacet() {
        return this.syntaxFacet;
    }

    /**
     * Gets the Stratego runtime facet.
     *
     * @return The Stratego runtime facet; or <code>null</code> when there is no Stratego runtime facet.
     */
    public @Nullable StrategoRuntimeFacet strategoRuntimeFacet() {
        return this.strategoRuntimeFacet;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(this.available) {
            sb.append("Found language component at ");
            sb.append(this.location);
            if(this.config != null) {
                sb.append(": ");
                sb.append(this.config.identifier());
            } else {
                sb.append(".");
            }
            sb.append("\n");
        } else {
            sb.append("Cannot create language component at ");
            sb.append(this.location);
            sb.append(".\n");
        }
        final boolean hasErrors = !Iterables.isEmpty(errors);
        final boolean hasExceptions = !Iterables.isEmpty(exceptions);

        if(hasErrors) {
            sb.append("The following errors occurred: \n");
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
            sb.append("The following exceptions occurred: \n");
            for(Throwable exception : exceptions) {
                sb.append("  ");
                sb.append(ExceptionUtils.getStackTrace(exception));
                sb.append('\n');
            }
        }

        return sb.toString();
    }
}
