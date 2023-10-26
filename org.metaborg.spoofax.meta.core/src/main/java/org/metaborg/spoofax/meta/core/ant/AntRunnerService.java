package org.metaborg.spoofax.meta.core.ant;

import java.net.URL;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.resource.IResourceService;


public class AntRunnerService implements IAntRunnerService {
    private final IResourceService resourceService;


    @jakarta.inject.Inject @javax.inject.Inject public AntRunnerService(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public IAntRunner get(FileObject antFile, FileObject baseDir, Map<String, String> properties,
        @Nullable URL[] classpaths, @Nullable BuildListener listener) {
        return new AntRunner(resourceService, antFile, baseDir, properties, classpaths, listener);
    }
}
