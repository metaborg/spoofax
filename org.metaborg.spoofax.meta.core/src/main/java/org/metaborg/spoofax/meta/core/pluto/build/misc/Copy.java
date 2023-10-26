package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jakarta.annotation.Nullable;

import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.builder.factory.BuilderFactoryFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class Copy extends Builder<Copy.Input, None> {
    public static class Input implements Serializable {
        private static final long serialVersionUID = 8710048518971598430L;

        public final File source;
        public final File destination;

        public final @Nullable Origin origin;
        public final @Nullable File baseDir;
        public final @Nullable File depDir;


        public Input(File source, File destination, @Nullable Origin origin, @Nullable File baseDir,
            @Nullable File depDir) {
            this.source = source;
            this.destination = destination;
            this.origin = origin;
            this.baseDir = baseDir;
            this.depDir = depDir;
        }


        public Input(File source, File destination, @Nullable Origin origin) {
            this(source, destination, origin, null, null);
        }
    }


    public static BuilderFactory<Input, None, Copy> factory = BuilderFactoryFactory.of(Copy.class, Input.class);


    public Copy(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, Copy, BuilderFactory<Input, None, Copy>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Copy " + input.source.getName();
    }

    @Override public File persistentPath(Input input) {
        final File depDir = input.depDir != null ? input.depDir : input.destination.getParentFile();
        final String name;
        if(input.baseDir != null) {
            final Path rel = FileCommands.getRelativePath(input.baseDir, input.source);
            name = rel.toString().replace(File.separatorChar, '_');
        } else {
            name = input.source.toString().replace(File.separatorChar, '_');
        }
        return new File(depDir, "copy." + name + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(input.origin);

        require(input.source);
        FileCommands.copyFile(input.source, input.destination, StandardCopyOption.COPY_ATTRIBUTES);
        provide(input.destination);

        return None.val;
    }
}
