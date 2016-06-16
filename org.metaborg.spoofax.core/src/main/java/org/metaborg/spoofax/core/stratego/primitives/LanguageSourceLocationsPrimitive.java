package org.metaborg.spoofax.core.stratego.primitives;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
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
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageSourceLocationsPrimitive(ILanguagePathService languagePathService,
        IProjectService projectService) {
        super("SSL_EXT_language_source_locations", 0, 1);
        this.languagePathService = languagePathService;
        this.projectService = projectService;
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

        // GTODO: require language identifier instead of language name
        final String languageName = Tools.asJavaString(tvars[0]);
        final Iterable<FileObject> sourceLocations = languagePathService.sourcePaths(project, languageName);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(FileObject sourceLocation : sourceLocations) {
            terms.add(factory.makeString(sourceLocation.getName().getURI()));
        }
        env.setCurrent(factory.makeList(terms));
        return true;
    }
}
