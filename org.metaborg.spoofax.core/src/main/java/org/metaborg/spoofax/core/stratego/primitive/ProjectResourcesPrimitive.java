package org.metaborg.spoofax.core.stratego.primitive;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.build.dependency.MissingDependencyException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.resource.IResourceService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import javax.inject.Inject;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {

    private final IDependencyService dependenceService;

    @Inject public ProjectResourcesPrimitive(IDependencyService dependencyService, IResourceService resourceService) {
        super("project_resources", resourceService);
        this.dependenceService = dependencyService;
    }

    @Override protected List<FileObject> locations(IContext context) throws MissingDependencyException {
        final Builder<FileObject> locations = ImmutableList.builder();
        locations.add(context.project().location());
        for(ILanguageComponent lang : dependenceService.sourceDeps(context.project())) {
            locations.add(lang.location());
        }
        return locations.build();
    }

}