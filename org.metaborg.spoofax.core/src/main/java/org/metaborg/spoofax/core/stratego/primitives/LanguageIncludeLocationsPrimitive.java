package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageIncludeLocationsPrimitive extends AbstractPrimitive {
    private static final Logger logger = LoggerFactory.getLogger(LanguageIncludeLocationsPrimitive.class);

    private final IResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageIncludeLocationsPrimitive(IResourceService resourceService,
        ILanguageService languageService, ILanguagePathService languagePathService, IProjectService projectService) {
        super("SSL_EXT_language_include_locations", 0, 1);
        this.resourceService = resourceService;
        this.languageService = languageService;
        this.languagePathService = languagePathService;
        this.projectService = projectService;
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        if(!Tools.isTermString(tvars[0])) {
            return false;
        }

        final ITermFactory factory = env.getFactory();
        final String languageName = Tools.asJavaString(tvars[0]);
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();
        final IProject project = projectService.get(context.location());
        if(project == null) {
            env.setCurrent(factory.makeList());
            return true;
        }

        final ILanguageImpl language = languageService.get(languageName);
        final Iterable<FileObject> includeLocations = languagePathService.includePaths(project, language.name());
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(FileObject includeLocation : includeLocations) {
            try {
                File localFile = resourceService.localPath(includeLocation);
                if(localFile == null) {
                    if(!includeLocation.exists()) {
                        warnNotExists(includeLocation);
                    }
                    localFile = resourceService.localFile(includeLocation);
                }

                terms.add(factory.makeString(localFile.getPath()));
            } catch(FileSystemException e) {
                warnNotExists(includeLocation);
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
