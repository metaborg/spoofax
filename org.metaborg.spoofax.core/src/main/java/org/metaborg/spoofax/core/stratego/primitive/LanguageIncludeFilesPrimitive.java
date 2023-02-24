package org.metaborg.spoofax.core.stratego.primitive;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class LanguageIncludeFilesPrimitive extends ASpoofaxContextPrimitive {
    private final ILanguageService languageService;
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageIncludeFilesPrimitive(ILanguageService languageService,
        ILanguagePathService languagePathService, IProjectService projectService) {
        super("language_include_files", 0, 1);
        this.languageService = languageService;
        this.languagePathService = languagePathService;
        this.projectService = projectService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException {
        if(!TermUtils.isString(tvars[0])) return null;

        final IProject project = projectService.get(context.location());
        if(project == null) {
            return factory.makeList();
        }

        // GTODO: require language identifier instead of language name
        final String languageName = TermUtils.toJavaString(tvars[0]);
        final ILanguage language = languageService.getLanguage(languageName);
        if(language == null) {
            final String message =
                String.format("Getting include files for %s failed, language could not be found", languageName);
            throw new MetaborgException(message);
        }
        final ILanguageImpl impl = language.activeImpl();
        if(impl == null) {
            final String message = String.format(
                "Getting include files for %s failed, no active language implementation could be found", languageName);
            throw new MetaborgException(message);
        }

        final Iterable<IdentifiedResource> includeFiles = languagePathService.includeFiles(project, impl);
        final List<IStrategoTerm> terms = new ArrayList<>();
        for(IdentifiedResource includeFile : includeFiles) {
            terms.add(factory.makeString(includeFile.resource.getName().getURI()));
        }
        return factory.makeList(terms);
    }
}
