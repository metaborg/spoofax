package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.file.FileAccess;
import org.metaborg.util.file.FileUtils;

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

    protected void processFileAccess(FileAccess access) {
        for(FileObject fileObject : access.reads()) {
            final File file = FileUtils.toFile(fileObject);
            require(file);
        }
        for(FileObject fileObject : access.writes()) {
            final File file = FileUtils.toFile(fileObject);
            provide(file);
        }
    }
}
