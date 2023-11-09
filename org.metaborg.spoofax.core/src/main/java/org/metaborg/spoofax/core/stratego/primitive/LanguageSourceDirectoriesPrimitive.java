package org.metaborg.spoofax.core.stratego.primitive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import org.spoofax.terms.util.TermUtils;

public class LanguageSourceDirectoriesPrimitive extends ASpoofaxContextPrimitive {
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @jakarta.inject.Inject @javax.inject.Inject public LanguageSourceDirectoriesPrimitive(ILanguagePathService languagePathService,
        IProjectService projectService) {
        super("language_source_directories", 0, 1);
        this.languagePathService = languagePathService;
        this.projectService = projectService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        if(!TermUtils.isString(tvars[0])) return null;

        final IProject project = projectService.get(context.location());
        if(project == null) {
            return factory.makeList();
        }

        // GTODO: require language identifier instead of language name
        final String languageName = TermUtils.toJavaString(tvars[0]);
        final Iterable<FileObject> sourceLocations = languagePathService.sourcePaths(project, languageName);
        final List<IStrategoTerm> terms = new ArrayList<>();
        for(FileObject sourceLocation : sourceLocations) {
            terms.add(factory.makeString(sourceLocation.getName().getURI()));
        }
        return factory.makeList(terms);
    }
}
