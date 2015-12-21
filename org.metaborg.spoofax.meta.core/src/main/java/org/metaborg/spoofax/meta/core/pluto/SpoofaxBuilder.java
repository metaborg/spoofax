package org.metaborg.spoofax.meta.core.pluto;

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


    protected void require(FileObject fileObject) {
        require(FileUtils.toFile(fileObject));
    }

    protected void provide(FileObject fileObject) {
        provide(FileUtils.toFile(fileObject));
    }

    protected void processFileAccess(FileAccess access) {
        for(FileObject fileObject : access.reads()) {
            require(fileObject);
        }
        for(FileObject fileObject : access.writes()) {
            provide(fileObject);
        }
    }
}
