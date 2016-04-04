package org.metaborg.spoofax.meta.core.stratego.primitives;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class LanguageSpecNamePrimitive extends AbstractPrimitive {
    private final IProjectService projectService;
    private final ISpoofaxLanguageSpecService languageSpecService;

    @Inject public LanguageSpecNamePrimitive(IProjectService projectService,
        ISpoofaxLanguageSpecService languageSpecService) {
        super("SSL_EXT_language_spec_name", 0, 0);

        this.projectService = projectService;
        this.languageSpecService = languageSpecService;
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();

        final FileObject location = context.location();
        final IProject project = projectService.get(location);
        if(project == null) {
            return false;
        }

        if(!languageSpecService.available(project)) {
            return false;
        }
        final ISpoofaxLanguageSpec languageSpec;
        try {
            languageSpec = languageSpecService.get(project);
        } catch(ConfigException e) {
            throw new InterpreterException("Unable to get language specification project at " + location, e);
        }
        if(languageSpec == null) {
            return false;
        }

        env.setCurrent(env.getFactory().makeString(languageSpec.config().name()));
        return true;
    }
}
