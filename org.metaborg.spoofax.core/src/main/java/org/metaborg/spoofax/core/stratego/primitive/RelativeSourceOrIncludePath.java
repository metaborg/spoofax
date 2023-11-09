package org.metaborg.spoofax.core.stratego.primitive;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import org.spoofax.terms.util.TermUtils;

public class RelativeSourceOrIncludePath extends ASpoofaxContextPrimitive {
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;
    private final IProjectService projectService;


    @jakarta.inject.Inject @javax.inject.Inject public RelativeSourceOrIncludePath(ILanguagePathService languagePathService, IResourceService resourceService,
            IProjectService projectService) {
        super("language_relative_source_or_include_path", 0, 1);
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
        this.projectService = projectService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) {
        if(!TermUtils.isString(tvars[0])) return null;
        if(!TermUtils.isString(current)) return null;

        final String path = TermUtils.toJavaString(current);
        final FileObject resource = resourceService.resolve(context.project().location(), path);

        FileObject base = context.location();
        final IProject project = projectService.get(context.location());
        if(project != null) {
            // GTODO: require language identifier instead of language name
            final String languageName = TermUtils.toJavaString(tvars[0]);
            final Iterable<FileObject> sourceLocations = languagePathService.sourceAndIncludePaths(project, languageName);
            for(FileObject sourceLocation : sourceLocations) {
                if(sourceLocation.getName().isDescendent(resource.getName())) {
                    base = sourceLocation;
                    break;
                }
            }
        }

        final String relativePath = ResourceUtils.relativeName(resource.getName(), base.getName(), true);
        return factory.makeString(relativePath);
    }
}
