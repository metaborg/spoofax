package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.paths.ILanguagePathService;
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

public class LanguageSourceLocationsPrimitive extends AbstractPrimitive {
    private static final Logger logger = LoggerFactory.getLogger(LanguageSourceLocationsPrimitive.class);

    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;
    private final IProjectService projectService;


    @Inject public LanguageSourceLocationsPrimitive(IResourceService resourceService, IProjectService projectService,
        ILanguagePathService languagePathService) {
        super("SSL_EXT_language_source_locations", 0, 1);
        this.projectService = projectService;
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
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
        final Iterable<FileObject> sourceLocations = languagePathService.sourcePaths(project, languageName);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(FileObject sourceLocation : sourceLocations) {
            try {
                if(!sourceLocation.exists()) {
                    logger.warn("Cannot add {} as source location, it does not exist", sourceLocation);
                    continue;
                }
                final File localFile = resourceService.localFile(sourceLocation);
                terms.add(factory.makeString(localFile.getPath()));
            } catch(FileSystemException e) {
                logger.warn("Cannot add {} as source location, it does not exist", sourceLocation);
            }
        }
        env.setCurrent(factory.makeList(terms));
        return true;
    }
}
