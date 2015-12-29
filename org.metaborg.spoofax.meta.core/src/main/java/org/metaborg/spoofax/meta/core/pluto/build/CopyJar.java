package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopyJar extends SpoofaxBuilder<CopyJar.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 8710048518971598430L;

        public final File externalJar;


        public Input(SpoofaxContext context, File externaljar) {
            super(context);
            this.externalJar = externaljar;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, CopyJar> factory = SpoofaxBuilderFactoryFactory.of(CopyJar.class,
        Input.class);


    public CopyJar(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, CopyJar, SpoofaxBuilderFactory<Input, None, CopyJar>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Copy external Jar";
    }

    @Override public File persistentPath(Input input) {
        if(input.externalJar != null) {
            final Path rel = FileCommands.getRelativePath(context.baseDir, input.externalJar);
            final String relName = rel.toString().replace(File.separatorChar, '_');
            return context.depPath("copy-jar." + relName + ".dep");
        }
        return context.depPath("copy-jar.dep");
    }

    @Override public None build(Input input) throws IOException {
        if(input.externalJar != null) {
            final File target = toFile(context.settings.getIncludeDirectory().resolveFile(input.externalJar.getName()));
            require(input.externalJar, LastModifiedStamper.instance);
            FileCommands.copyFile(input.externalJar, target, StandardCopyOption.COPY_ATTRIBUTES);
            provide(target);
        }
        return None.val;
    }
}
