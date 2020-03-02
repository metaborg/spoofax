package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class LanguageIncludeDirectoriesPrimitive extends ASpoofaxContextPrimitive {
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageIncludeDirectoriesPrimitive(ILanguagePathService languagePathService,
        IProjectService projectService) {
        super("language_include_directories", 0, 1);
        this.languagePathService = languagePathService;
        this.projectService = projectService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        if(!TermUtils.isString(tvars[0])) return null;

        final IProject project = projectService.get(context.location());
        if(project == null) {
            return factory.makeList();
        }

        // GTODO: require language identifier instead of language name
        final String languageName = TermUtils.toJavaString(tvars[0]);
        final Iterable<FileObject> includeLocations = languagePathService.includePaths(project, languageName);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(FileObject includeLocation : includeLocations) {
            terms.add(factory.makeString(includeLocation.getName().getURI()));
        }
        return factory.makeList(terms);
    }
}
