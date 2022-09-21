package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.misc.CopyPattern;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryModifiedStamper;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;

public class PackageBuilder extends SpoofaxBuilder<PackageBuilder.Input, PackageBuilder.Output> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final Origin origin;

        public final String languageId;

        public final StrategoFormat strFormat;


        public Input(SpoofaxContext context, String languageId, Origin origin, StrategoFormat strFormat) {
            super(context);
            this.origin = origin;
            this.languageId = languageId;
            this.strFormat = strFormat;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = -3404709430588259993L;

        public final Origin jarBuilderOrigin;

        public Output(Origin jarBuilderOrigin) {
            this.jarBuilderOrigin = jarBuilderOrigin;
        }

        @Override public int hashCode() {
            return jarBuilderOrigin.hashCode();
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(!jarBuilderOrigin.equals(other.jarBuilderOrigin))
                return false;
            return true;
        }
    }

    public static SpoofaxBuilderFactory<Input, Output, PackageBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(PackageBuilder.class, Input.class);


    public PackageBuilder(Input input) {
        super(input);
    }


    public static BuildRequest<Input, Output, PackageBuilder, SpoofaxBuilderFactory<Input, Output, PackageBuilder>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Package language implementation";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("package.dep");
    }

    @Override protected Output build(Input input) throws Throwable {
        requireBuild(input.origin);

        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
        final File targetClassesDir = toFile(paths.targetClassesDir());
        final File str2libsDir = toFile(paths.str2libsDir());

        final File strJavaTransDir = toFile(paths.strSrcGenJavaTransDir(input.languageId));
        final File strClassesTransDir = toFile(paths.strTargetClassesTransDir(input.languageId));

        final Origin copyPatternOrigin;
        if(strJavaTransDir.exists()) {
            // Copy .pp.af and .tbl to JAR target directory, so that they get included in the JAR file.
            // Required for being able to import-term those files from Stratego code.
            final CopyPattern.Input copyPatternInput =
                new CopyPattern.Input(strJavaTransDir, strClassesTransDir, ".+\\.(?:tbl|pp\\.af)", input.origin, context.baseDir, context.depDir);
            copyPatternOrigin = CopyPattern.origin(copyPatternInput);
            requireBuild(copyPatternOrigin);
        } else {
            copyPatternOrigin = null;
        }

        final String jarName = "stratego.jar";
        final File jarFile = FileUtils.getFile(targetMetaborgDir, jarName);
        final File depPath = FileUtils.getFile(context.depDir, jarName + ".dep");
        final Origin origin = jar(jarFile, copyPatternOrigin, depPath, targetClassesDir, str2libsDir);

        return new Output(origin);
    }

    public Origin jar(File jarFile, @Nullable Origin origin, @Nullable File depPath, File... paths)
        throws IOException {
        final Collection<JarBuilder.Entry> fileEntries = new ArrayList<>();

        for(File baseDir : paths) {
            // N.B. this only checks the modified time of the dir, not subdirs which we do traverse!
            require(baseDir, new DirectoryModifiedStamper());
            final Collection<File> files = findFiles(baseDir);
            for(final File classFile : files) {
                final String relative = relativize(classFile, baseDir);
                // Ignore files that are not relative to the base directory.
                if(relative != null) {
                    if(classFile.isDirectory()) {
                        // So we also require all subdirs we find
                        require(classFile, new DirectoryModifiedStamper());
                    }
                    // Convert \ to / on Windows; ZIP/JAR files must use / for paths.
                    // HACK: this should be fixed in the JarBuilder.
                    final String forwardslashRelative = relative.replace('\\', '/');
                    fileEntries.add(new JarBuilder.Entry(forwardslashRelative, classFile));
                }
            }
        }

        final BuildRequest<?, ?, ?, ?> buildRequest =
            new BuildRequest<>(JarBuilder.factory, new JarBuilder.Input(jarFile, fileEntries, origin, depPath));
        final Origin jarOrigin = Origin.from(buildRequest);
        requireBuild(jarOrigin);
        return jarOrigin;
    }

    private @Nullable String relativize(File path, File base) {
        final String relative = FilenameUtils.normalize(base.toPath().relativize(path.toPath()).toString());
        if(relative == null || relative.equals("")) {
            return null;
        }
        return relative;
    }

    private Collection<File> findFiles(File directory) {
        if(!directory.isDirectory()) {
            return Collections.emptyList();
        }
        return FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    }
}
