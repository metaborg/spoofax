package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.misc.CopyPattern;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryModifiedStamper;
import org.metaborg.util.resource.FileSelectorUtils;
import org.metaborg.util.resource.ZipArchiver;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.FileExistsStamper;

public class PackageBuilder extends SpoofaxBuilder<PackageBuilder.Input, OutputTransient<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final Origin generateSourcesOrigin;

        public final StrategoFormat strFormat;
        public final @Nullable File strJavaStratFile;

        public final Iterable<File> strJavaStratIncludeDirs;

        public final Iterable<IExportConfig> exports;
        public final LanguageIdentifier languageIdentifier;


        public Input(SpoofaxContext context, Origin generateSourcesOrigin, StrategoFormat strFormat,
            @Nullable File strJavaStratFile, Iterable<File> strJavaStratIncludeDirs, Iterable<IExportConfig> exports,
            LanguageIdentifier languageIdentifier) {
            super(context);
            this.generateSourcesOrigin = generateSourcesOrigin;
            this.strFormat = strFormat;
            this.strJavaStratFile = strJavaStratFile;
            this.strJavaStratIncludeDirs = strJavaStratIncludeDirs;
            this.exports = exports;
            this.languageIdentifier = languageIdentifier;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputTransient<File>, PackageBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(PackageBuilder.class, Input.class);


    public PackageBuilder(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputTransient<File>, PackageBuilder, SpoofaxBuilderFactory<Input, OutputTransient<File>, PackageBuilder>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Package";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("package.dep");
    }

    @Override protected OutputTransient<File> build(Input input) throws Throwable {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
        final File targetClassesDir = toFile(paths.targetClassesDir());

        final Origin.Builder packageOriginBuilder = Origin.Builder();
        packageOriginBuilder.add(input.generateSourcesOrigin);

        if(input.strFormat == StrategoFormat.jar) {
            final File strJavaTransDir = toFile(paths.strSrcGenJavaTransDir());
            final File strClassesTransDir = toFile(paths.strTargetClassesTransDir());

            // Copy .pp.af and .tbl to JAR target directory, so that they get included in the JAR file.
            // Required for being able to import-term those files from Stratego code.
            final CopyPattern.Input copyPatternInput = new CopyPattern.Input(strJavaTransDir, strClassesTransDir,
                ".+\\.(?:tbl|pp\\.af)", input.generateSourcesOrigin, context.baseDir, context.depDir);
            final Origin copyPatternOrigin = CopyPattern.origin(copyPatternInput);
            requireBuild(copyPatternOrigin);

            final String jarName = "stratego.jar";
            final File jarFile = FileUtils.getFile(targetMetaborgDir, jarName);
            final File depPath = FileUtils.getFile(context.depDir, jarName + ".dep");
            final Origin origin = jar(jarFile, targetClassesDir, copyPatternOrigin, depPath, strClassesTransDir);
            packageOriginBuilder.add(origin);
        }

        if(input.strJavaStratFile != null) {
            require(input.strJavaStratFile, FileExistsStamper.instance);
            if(!input.strJavaStratFile.exists()) {
                throw new IOException(
                    "Main Stratego Java strategies file at " + input.strJavaStratFile + " does not exist");
            }

            final String jarName = "stratego-javastrat.jar";
            final File jarFile = FileUtils.getFile(targetMetaborgDir, jarName);
            final File depPath = FileUtils.getFile(context.depDir, jarName + ".dep");
            final Origin origin = jar(jarFile, targetClassesDir, null, depPath, input.strJavaStratIncludeDirs);
            packageOriginBuilder.add(origin);
        }

        final Origin packageOrigin = packageOriginBuilder.get();
        requireBuild(packageOrigin);

        report("Packaging language implementation archive");
        final ZipArchiver zipArchiver = new ZipArchiver();
        final FileObject root = paths.root();
        zipArchiver.addFilesTo(root.getName(), paths.iconsDir(), FileSelectorUtils.all());
        zipArchiver.addFilesTo(root.getName(), paths.targetMetaborgDir(),
            FileSelectorUtils.not(FileSelectorUtils.ant("*.dep")));
        zipArchiver.addFileTo(root.getName(), paths.mbComponentConfigFile());
        for(IExportConfig export : input.exports) {
            export.accept(new IExportVisitor() {
                @Override public void visit(ResourceExport export) {
                    addFiles(export, export.directory, export.includes, export.excludes);
                }

                @Override public void visit(LangDirExport export) {
                    addFiles(export, export.directory, export.includes, export.excludes);
                }

                @Override public void visit(LangFileExport export) {
                    try {
                        final FileObject file = paths.root().resolveFile(export.file);
                        zipArchiver.addFile(export.file, file);
                    } catch(IOException e) {
                        report("Unable to package export: " + export);
                    }
                }

                private void addFiles(IExportConfig export, String directory, Iterable<String> includes,
                    Iterable<String> excludes) {
                    try {
                        final FileObject dir = paths.root().resolveFile(directory);
                        final FileSelector includesSelector;
                        if(Iterables.isEmpty(includes)) {
                            includesSelector = FileSelectorUtils.all();
                        } else {
                            includesSelector = FileSelectorUtils.ant(includes);
                        }
                        final FileSelector excludesSelector;
                        if(Iterables.isEmpty(excludes)) {
                            excludesSelector = FileSelectorUtils.none();
                        } else {
                            excludesSelector = FileSelectorUtils.ant(excludes);
                        }
                        zipArchiver.addFilesTo(paths.root().getName(), dir,
                            FileSelectorUtils.includeExclude(includesSelector, excludesSelector));
                    } catch(IOException e) {
                        report("Unable to package export: " + export);
                    }
                }
            });
        }
        final FileObject spxArchiveFile = paths.spxArchiveFile(input.languageIdentifier.toFileString());
        zipArchiver.build(spxArchiveFile, this);

        return OutputTransient.of(toFile(spxArchiveFile));
    }

    public Origin jar(File jarFile, File baseDir, @Nullable Origin origin, @Nullable File depPath, File... paths)
        throws IOException {
        return jar(jarFile, baseDir, origin, depPath, Lists.newArrayList(paths));
    }

    public Origin jar(File jarFile, File baseDir, @Nullable Origin origin, @Nullable File depPath, Iterable<File> paths)
        throws IOException {
        final Collection<JarBuilder.Entry> fileEntries = Lists.newLinkedList();

        for(File path : paths) {
            require(path, new DirectoryModifiedStamper());
            final Collection<File> files = findFiles(path);
            for(final File classFile : files) {
                final String relative = relativize(classFile, baseDir);
                // Ignore files that are not relative to the base directory.
                if(relative != null) {
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
