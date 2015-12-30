package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.util.file.FileAccess;

import build.pluto.builder.Builder;
import build.pluto.output.Output;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

abstract public class SpoofaxBuilder<In extends SpoofaxInput, Out extends Output> extends Builder<In, Out> {
    protected final SpoofaxContext context;


    public SpoofaxBuilder(In input) {
        super(input);
        this.context = input.context;
    }


    @Override protected Stamper defaultStamper() {
        return SpoofaxContext.BETTER_STAMPERS ? FileHashStamper.instance : LastModifiedStamper.instance;
    }


    protected File toFile(FileObject fileObject) {
        return context.toFile(fileObject);
    }

    protected File toFileReplicate(FileObject fileObject) {
        return context.toFileReplicate(fileObject);
    }

    protected void require(FileObject fileObject) {
        require(toFile(fileObject));
    }

    protected void provide(FileObject fileObject) {
        provide(toFile(fileObject));
    }

    protected void processFileAccess(FileAccess access) {
        for(FileObject fileObject : access.reads()) {
            require(fileObject);
        }
        for(FileObject fileObject : access.writes()) {
            provide(fileObject);
        }
    }

    protected ResourceAgentTracker newResourceTracker(String... excludePatterns) {
        return context.newResourceTracker(excludePatterns);
    }
}
