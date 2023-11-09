package org.metaborg.spoofax.meta.core.pluto.build.misc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;

import jakarta.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryModifiedStamper;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.builder.factory.BuilderFactoryFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class CopyPattern extends Builder<CopyPattern.Input, None> {
    public static class Input implements Serializable {
        private static final long serialVersionUID = 8710048518971598430L;

        public final File srcDir;
        public final File dstDir;
        public final String pattern;

        public final @Nullable Origin origin;
        public final @Nullable File baseDir;
        public final @Nullable File depDir;


        public Input(File srcDir, File dstDir, String pattern, @Nullable Origin origin, @Nullable File baseDir,
            @Nullable File depDir) {
            this.srcDir = srcDir;
            this.dstDir = dstDir;
            this.pattern = pattern;

            this.origin = origin;
            this.baseDir = baseDir;
            this.depDir = depDir;
        }


        public Input(File srcDir, File dstDir, String pattern, @Nullable Origin origin) {
            this(srcDir, dstDir, pattern, origin, null, null);
        }
    }


    public static BuilderFactory<Input, None, CopyPattern> factory =
        BuilderFactoryFactory.of(CopyPattern.class, Input.class);


    public CopyPattern(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, CopyPattern, BuilderFactory<Input, None, CopyPattern>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        final String srcName;
        if(input.baseDir != null) {
            srcName = FileCommands.getRelativePath(input.baseDir, input.srcDir).toString();
        } else {
            srcName = input.srcDir.getName();
        }

        final String dstName;
        if(input.baseDir != null) {
            dstName = FileCommands.getRelativePath(input.baseDir, input.dstDir).toString();
        } else {
            dstName = input.dstDir.getName();
        }

        return "Copy '" + input.pattern + "' from " + srcName + " to " + dstName;
    }

    @Override public File persistentPath(Input input) {
        final File depDir = input.depDir != null ? input.depDir : input.dstDir.getParentFile();
        final String srcName;
        if(input.baseDir != null) {
            final Path rel = FileCommands.getRelativePath(input.baseDir, input.srcDir);
            srcName = rel.toString().replace(File.separatorChar, '_');
        } else {
            srcName = input.srcDir.toString().replace(File.separatorChar, '_');
        }
        final String dstName;
        if(input.baseDir != null) {
            final Path rel = FileCommands.getRelativePath(input.baseDir, input.dstDir);
            dstName = rel.toString().replace(File.separatorChar, '_');
        } else {
            dstName = input.dstDir.toString().replace(File.separatorChar, '_');
        }
        return new File(depDir, "copypattern." + srcName + "." + dstName + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(input.origin);

        require(input.srcDir, new DirectoryModifiedStamper());

        final Collection<File> files =
            FileUtils.listFiles(input.srcDir, new RegexFileFilter(input.pattern), FalseFileFilter.INSTANCE);
        for(File file : files) {
            require(file);
            final File dstFile = new File(input.dstDir, file.getName());
            FileUtils.copyFile(file, dstFile);
            provide(dstFile);
        }

        return None.val;
    }
}
