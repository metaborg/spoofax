package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.util.file.IFileAccess;

import build.pluto.builder.Builder;
import build.pluto.output.Output;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

abstract public class SpoofaxBuilder<In extends SpoofaxInput, Out extends Output> extends Builder<In, Out>
    implements IFileAccess {
    protected final SpoofaxContext context;
    protected transient SpoofaxLangSpecCommonPaths paths;


    public SpoofaxBuilder(In input) {
        super(input);
        this.context = input.context;
        this.paths = new SpoofaxLangSpecCommonPaths(context.base);
    }


    @Override protected Stamper defaultStamper() {
        return SpoofaxContext.BETTER_STAMPERS ? FileHashStamper.instance : LastModifiedStamper.instance;
    }


    @Override public void read(FileObject file) {
        require(toFile(file));
    }

    @Override public void write(FileObject file) {
        provide(toFile(file));
    }


    protected File toFile(FileObject fileObject) {
        return context.toFile(fileObject);
    }

    protected File toFileReplicate(FileObject fileObject) {
        return context.toFileReplicate(fileObject);
    }


    protected ResourceAgentTracker newResourceTracker(String... excludePatterns) {
        return context.newResourceTracker(excludePatterns);
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.paths = new SpoofaxLangSpecCommonPaths(context.base);
    }
}
