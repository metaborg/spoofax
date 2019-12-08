package org.metaborg.spoofax.core.stratego.primitive;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.resource.IResourceService;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {

    @Inject public ProjectResourcesPrimitive(IResourceService resourceService) {
        super("project_resources", resourceService);
    }

    @Override protected List<FileObject> locations(IContext context) {
        return ImmutableList.of(context.project().location());
    }

}