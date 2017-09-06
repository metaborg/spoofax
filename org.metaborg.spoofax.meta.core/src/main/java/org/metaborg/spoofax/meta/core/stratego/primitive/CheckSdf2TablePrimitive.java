package org.metaborg.spoofax.meta.core.stratego.primitive;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CheckSdf2TablePrimitive extends AbstractPrimitive {
    private static final ILogger logger = LoggerUtils.logger(CheckSdf2TablePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;

    private final IProjectService projectService;

    @Inject public CheckSdf2TablePrimitive(IProjectService projectService) {
        super("SSL_EXT_check_sdf2_table", 0, 0);
 
        this.projectService = projectService;
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();

        final FileObject location = context.location();
        final IProject project = projectService.get(location);
        if(project == null) {
            return false;
        }

        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return false;
        }
        final ISpoofaxLanguageSpecService languageSpecService = languageSpecServiceProvider.get();
        if(!languageSpecService.available(project)) {
            return false;
        }
        final ISpoofaxLanguageSpec languageSpec;
        try {
            languageSpec = languageSpecService.get(project);
        } catch(ConfigException e) {
            throw new InterpreterException("Unable to get language specification name for " + location, e);
        }
        if(languageSpec == null) {
            return false;
        }

        env.setCurrent(env.getFactory().makeString(languageSpec.config().sdf2tableVersion().toString()));
        return true;
    }
}
