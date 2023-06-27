package org.metaborg.spoofax.core.stratego.primitive;

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

import com.google.common.collect.Lists;
import javax.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class LanguageSourceFilesPrimitive extends ASpoofaxContextPrimitive {
    private final ILanguageService languageService;
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageSourceFilesPrimitive(ILanguageService languageService,
        ILanguagePathService languagePathService, IProjectService projectService) {
        super("language_source_files", 0, 1);
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

        final Iterable<IdentifiedResource> sourceFiles = languagePathService.sourceFiles(project, impl);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(IdentifiedResource sourceFile : sourceFiles) {
            terms.add(factory.makeString(sourceFile.resource.getName().getURI()));
        }
        return factory.makeList(terms);
    }
}
