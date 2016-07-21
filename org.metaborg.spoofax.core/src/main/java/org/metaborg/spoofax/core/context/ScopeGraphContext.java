package org.metaborg.spoofax.core.context;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContextInternal;
import org.metaborg.core.context.ITemporaryContextInternal;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.concurrent.NullClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class ScopeGraphContext implements IScopeGraphContext, IContextInternal, ITemporaryContextInternal {
    private static final ILogger logger = LoggerUtils.logger(ScopeGraphContext.class);

    private final ContextIdentifier identifier;
    private final Injector injector;

    private final Collection<FileObject> sources = Lists.newArrayList();
    private IStrategoTerm initial = null;
    
    public ScopeGraphContext(Injector injector, ContextIdentifier identifier) {
        this.identifier = identifier;
        this.injector = injector;
    }


    @Override
    public FileObject location() {
        return identifier.location;
    }

    @Override
    public IProject project() {
        return identifier.project;
    }

    @Override
    public ILanguageImpl language() {
        return identifier.language;
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public IClosableLock read() {
        return new NullClosableLock();
    }

    @Override
    public IClosableLock write() {
        return new NullClosableLock();
    }

    @Override
    public void persist() throws IOException {
        logger.warn("ScopeGraphContext.persist");
    }

    @Override
    public void reset() throws IOException {
        logger.warn("ScopeGraphContext.reset");
        this.sources.clear();
        this.initial = null;
    }

    @Override
    public void close() {
        logger.warn("ScopeGraphContext.close");
    }

    @Override
    public ContextIdentifier identifier() {
        return identifier;
    }

    @Override
    public void init() {
        logger.warn("ScopeGraphContext.init");
    }

    @Override
    public void load() {
        logger.warn("ScopeGraphContext.load");
    }

    @Override
    public void unload() {
        logger.warn("ScopeGraphContext.unload");
    }

    @Override
    public @Nullable IStrategoTerm getInitial() {
        return initial;
    }
    
    public void setInitial(IStrategoTerm initial) {
        if(this.initial != null) {
            logger.warn("Should only initialize once.");
        }
        this.initial = initial;
    }
    
    @Override
    public Collection<FileObject> sources() {
        return sources;
    }

}
