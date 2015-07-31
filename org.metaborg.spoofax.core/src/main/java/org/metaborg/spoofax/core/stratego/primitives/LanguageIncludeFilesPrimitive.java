package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguageIncludeFilesPrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguagePathService languagePathService;
    private final IProjectService projectService;


    @Inject public LanguageIncludeFilesPrimitive(ILanguageService languageService, IResourceService resourceService,
        ILanguagePathService languagePathService, IProjectService projectService) {
        super("SSL_EXT_language_include_files", 0, 1);
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
        org.metaborg.core.context.IContext context = (org.metaborg.core.context.IContext) env.contextObject();
        final IProject project = projectService.get(context.location());
        if(project == null) {
            env.setCurrent(factory.makeList());
            return true;
        }

        // GTODO: require language identifier instead of language name
        final String languageName = Tools.asJavaString(tvars[0]);
        final ILanguage language = languageService.getLanguage(languageName);
        if(language == null) {
            final String message =
                String.format("Getting include files for %s failed, language could not be found", languageName);
            throw new InterpreterException(message);
        }
        final ILanguageImpl impl = language.activeImpl();
        if(impl == null) {
            final String message =
                String.format("Getting include files for %s failed, no active language implementation could be found",
                    languageName);
            throw new InterpreterException(message);
        }

        final Iterable<IdentifiedResource> includeFiles = languagePathService.includeFiles(project, impl);
        final List<IStrategoTerm> terms = Lists.newArrayList();
        for(IdentifiedResource includeFile : includeFiles) {
            final FileObject file = includeFile.resource;
            final File localFile = resourceService.localFile(file);
            terms.add(factory.makeString(localFile.getPath()));
        }
        env.setCurrent(factory.makeList(terms));
        return true;
    }
}
