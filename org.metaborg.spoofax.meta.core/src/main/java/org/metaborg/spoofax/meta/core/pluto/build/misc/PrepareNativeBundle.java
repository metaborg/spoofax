package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.ExecutableCommandStrategy;
import org.metaborg.spoofax.nativebundle.NativeBundle;

import build.pluto.output.OutputTransient;
import build.pluto.stamp.LastModifiedStamper;

public class PrepareNativeBundle
    extends SpoofaxBuilder<PrepareNativeBundle.Input, OutputTransient<PrepareNativeBundle.Output>> {
    /**
     * Empty input class to ensure that a new input instance is always created, which is always exactly the same, such
     * that this builder is only executed once.
     */
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4515278489604797843L;

        public Input(SpoofaxContext context) {
            super(context);
        }
    }

    public static class Output implements Serializable {
        private static final long serialVersionUID = -6018464107000421068L;

        public final transient ExecutableCommandStrategy sdf2table;
        public final transient ExecutableCommandStrategy implodePT;


        public Output(ExecutableCommandStrategy sdf2table, ExecutableCommandStrategy implodePT) {
            this.sdf2table = sdf2table;
            this.implodePT = implodePT;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputTransient<PrepareNativeBundle.Output>, PrepareNativeBundle> factory =
        SpoofaxBuilderFactoryFactory.of(PrepareNativeBundle.class, Input.class);


    public PrepareNativeBundle(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Prepare native executables";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("native-executables.dep");
    }

    @Override public OutputTransient<Output> build(Input input) throws IOException {
        final URI nativeBundleURI = NativeBundle.getNativeDirectory();
        final FileObject nativeBundleLocation = context.resourceService().resolve(nativeBundleURI);
        final File nativeBundleDir = toFileReplicate(nativeBundleLocation);
        restoreExecutablePermissions(nativeBundleDir);
        final File sdf2TableFile = new File(nativeBundleDir, NativeBundle.getSdf2TableName());
        final File implodePtFile = new File(nativeBundleDir, NativeBundle.getImplodePTName());

        provide(sdf2TableFile, LastModifiedStamper.instance);
        provide(implodePtFile, LastModifiedStamper.instance);

        return OutputTransient.of(new Output(new ExecutableCommandStrategy("sdf2table", sdf2TableFile),
            new ExecutableCommandStrategy("implodePT", implodePtFile)));
    }

    private static void restoreExecutablePermissions(File directory) {
        for(File fileOrDirectory : directory.listFiles()) {
            if(fileOrDirectory.isDirectory()) {
                restoreExecutablePermissions(fileOrDirectory);
            } else {
                if(!fileOrDirectory.canExecute()) {
                    fileOrDirectory.setExecutable(true);
                }
            }
        }
    }
}
