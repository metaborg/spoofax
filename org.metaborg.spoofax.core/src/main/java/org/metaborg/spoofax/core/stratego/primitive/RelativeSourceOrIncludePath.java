package org.metaborg.spoofax.core.stratego.primitive;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class RelativeSourceOrIncludePath extends ASpoofaxContextPrimitive {
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;
    private final IProjectService projectService;


    @Inject public RelativeSourceOrIncludePath(ILanguagePathService languagePathService, IResourceService resourceService,
            IProjectService projectService) {
        super("language_relative_source_or_include_path", 0, 1);
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
        this.projectService = projectService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) {
        if(!Tools.isTermString(tvars[0])) {
            return null;
        }
        if(!Tools.isTermString(current)) {
            return null;
        }

        final String path = Tools.asJavaString(current);
        final FileObject resource = resourceService.resolve(path);

        FileObject base = context.location();
        final IProject project = projectService.get(context.location());
        if(project != null) {
            // GTODO: require language identifier instead of language name
            final String languageName = Tools.asJavaString(tvars[0]);
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
