package org.metaborg.spoofax.meta.core.ant;

import java.net.URL;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildListener;
import org.metaborg.spoofax.core.resource.IResourceService;

import com.google.inject.Inject;

public class AntRunnerService implements IAntRunnerService {
    private final IResourceService resourceService;


    @Inject public AntRunnerService(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public IAntRunner get(FileObject antFile, FileObject baseDir, Map<String, String> properties,
        @Nullable URL[] classpaths, @Nullable BuildListener listener) {
        return new AntRunner(resourceService, antFile, baseDir, properties, classpaths, listener);
    }
}
