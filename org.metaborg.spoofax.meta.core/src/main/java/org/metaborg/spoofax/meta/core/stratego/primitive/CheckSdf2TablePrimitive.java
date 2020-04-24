package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths;
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

public class CheckSdf2TablePrimitive extends AbstractPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(CheckSdf2TablePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;

    private final IProjectService projectService;
    private final ILanguagePathService languagePathService;

    @Inject public CheckSdf2TablePrimitive(IProjectService projectService, ILanguagePathService languagePathService) {
        super("SSL_EXT_check_sdf2_table", 0, 0);

        this.projectService = projectService;
        this.languagePathService = languagePathService;
    }

    @Override public void close() {
        languageSpecServiceProvider = null;
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

        boolean metaSDFfile = false;
        try {
            final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(languageSpec.location());
            final Iterable<FileObject> sdfRoots =
                languagePathService.sourcePaths(project, SpoofaxConstants.LANG_SDF_NAME);

            List<String> sdfMetaModules = languageSpec.config().sdfMetaFiles();

            for(String sdfMetaModule : sdfMetaModules) {
                final FileObject sdfMetaFileCandidate = paths.findSyntaxMainFile(sdfRoots, sdfMetaModule);
                if(sdfMetaFileCandidate != null && sdfMetaFileCandidate.exists()) {
                    metaSDFfile = true;
                }
            }
        } catch(FileSystemException fse) {
            metaSDFfile = false;
        }

        if(!languageSpec.config().sdfEnabled()) {
            env.setCurrent(env.getFactory().makeString("disabled"));
            return true;
        }

        if(metaSDFfile) {
            env.setCurrent(env.getFactory().makeString("mixin-grammar"));
            return true;
        }

        env.setCurrent(env.getFactory().makeString(languageSpec.config().sdf2tableVersion().toString()));
        return true;
    }
}
