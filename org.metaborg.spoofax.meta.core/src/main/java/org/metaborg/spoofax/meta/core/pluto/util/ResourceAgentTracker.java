package org.metaborg.spoofax.meta.core.pluto.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;

public class ResourceAgentTracker {
    private final ResourceAgent resourceAgent;
    private final ByteArrayOutputStream stdoutLog = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderrLog = new ByteArrayOutputStream();


    public ResourceAgentTracker(IResourceService resourceService, FileObject initialDir, String... excludePatterns) {
        this(resourceService, initialDir, ResourceAgent.defaultStdout(excludePatterns), ResourceAgent
            .defaultStderr(excludePatterns));
    }

    public ResourceAgentTracker(IResourceService resourceService, FileObject initialDir, OutputStream stdoutStream,
        OutputStream stderrStream) {
        final TeeOutputStream stdout = new TeeOutputStream(stdoutStream, stdoutLog);
        final TeeOutputStream stderr = new TeeOutputStream(stderrStream, stderrLog);
        this.resourceAgent = new ResourceAgent(resourceService, initialDir, stdout, stderr);
    }


    public ResourceAgent agent() {
        return resourceAgent;
    }

    public String stdout() {
        return stdoutLog.toString();
    }

    public String stderr() {
        return stderrLog.toString();
    }
}
