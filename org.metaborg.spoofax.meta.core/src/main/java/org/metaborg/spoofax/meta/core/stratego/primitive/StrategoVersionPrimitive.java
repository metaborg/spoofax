package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class StrategoVersionPrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoVersionPrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;

    private final IProjectService projectService;

    @Inject public StrategoVersionPrimitive(IProjectService projectService) {
        super("stratego_version", 0, 0);

        this.projectService = projectService;
    }

    @Override public void close() {
        languageSpecServiceProvider = null;
    }

    @Nullable @Override
    protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final FileObject location = context.location();
        final IProject project = projectService.get(location);
        if(project == null) {
            return null;
        }

        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return null;
        }
        final ISpoofaxLanguageSpecService languageSpecService = languageSpecServiceProvider.get();
        if(!languageSpecService.available(project)) {
            return null;
        }
        final ISpoofaxLanguageSpec languageSpec;
        try {
            languageSpec = languageSpecService.get(project);
        } catch(ConfigException e) {
            throw new MetaborgException("Unable to get language specification name for " + location, e);
        }
        if(languageSpec == null) {
            return null;
        }

        switch(languageSpec.config().strVersion()) {
            case v1:
                return factory.makeString("1");
            case v2:
                return factory.makeString("2");
            default:
                return null;
        }
    }
}
