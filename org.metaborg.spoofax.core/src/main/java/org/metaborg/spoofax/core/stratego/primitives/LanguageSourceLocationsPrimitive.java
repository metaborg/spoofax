package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.paths.INewLanguagePathService;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageSourceLocationsPrimitive extends AbstractPrimitive {
    private static final ILogger logger = LoggerUtils.logger(LanguageSourceLocationsPrimitive.class);

    private final IResourceService resourceService;
    private final INewLanguagePathService languagePathService;
    private final IProjectService projectService;
    private final ILanguageSpecService languageSpecService;


    @Inject public LanguageSourceLocationsPrimitive(IResourceService resourceService,
                                                    INewLanguagePathService languagePathService, IProjectService projectService, ILanguageSpecService languageSpecService) {
        super("SSL_EXT_language_source_locations", 0, 1);
        this.resourceService = resourceService;
        this.languagePathService = languagePathService;
        this.projectService = projectService;
        this.languageSpecService = languageSpecService;
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        if(!Tools.isTermString(tvars[0])) {
            return false;
        }

        final ITermFactory factory = env.getFactory();
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();
        if(context == null) {
            env.setCurrent(factory.makeList());
            return true;
        }

        final IProject project = projectService.get(context.location());
        if(project == null) {
            env.setCurrent(factory.makeList());
            return true;
        }

        final ILanguageSpec languageSpec = languageSpecService.get(project);
        if (languageSpec == null) {
            env.setCurrent(factory.makeList());
            return true;
        }

        // GTODO: require language identifier instead of language name
        final String languageName = Tools.asJavaString(tvars[0]);
        final Iterable<FileObject> sourceLocations = languagePathService.sourcePaths(languageSpec, languageName);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(FileObject sourceLocation : sourceLocations) {
            try {
                File localFile = resourceService.localPath(sourceLocation);
                if(localFile == null) {
                    if(!sourceLocation.exists()) {
                        warnNotExists(sourceLocation);
                    }
                    localFile = resourceService.localFile(sourceLocation);
                }

                terms.add(factory.makeString(localFile.getPath()));
            } catch(FileSystemException e) {
                warnNotExists(sourceLocation);
            }
        }
        env.setCurrent(factory.makeList(terms));
        return true;
    }


    private void warnNotExists(FileObject location) {
        logger.warn("Cannot add source location {}, it is not located on the local file system and does not exist",
            location);
    }
}
