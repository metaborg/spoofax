package org.metaborg.spoofax.core.stratego.primitive;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.MissingDependencyException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.collection.ImList;

import com.google.inject.Inject;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {

    private final IDependencyService dependenceService;

    @Inject public ProjectResourcesPrimitive(IDependencyService dependencyService, IResourceService resourceService) {
        super("project_resources", resourceService);
        this.dependenceService = dependencyService;
    }

    @Override protected List<FileObject> locations(IContext context) throws MissingDependencyException {
        final ImList.Mutable<FileObject> locations = ImList.Mutable.of();
        locations.add(context.project().location());
        for(ILanguageComponent lang : dependenceService.sourceDeps(context.project())) {
            locations.add(lang.location());
        }
        return locations.freeze();
    }

}