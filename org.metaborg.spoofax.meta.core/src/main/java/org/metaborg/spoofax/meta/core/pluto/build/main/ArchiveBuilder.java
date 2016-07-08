package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryModifiedStamper;
import org.metaborg.util.resource.FileSelectorUtils;
import org.metaborg.util.resource.ZipArchiver;

import com.google.common.collect.Iterables;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputTransient;

public class ArchiveBuilder extends SpoofaxBuilder<ArchiveBuilder.Input, OutputTransient<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final Origin origin;

        public final Iterable<IExportConfig> exports;
        public final LanguageIdentifier languageIdentifier;


        public Input(SpoofaxContext context, Origin origin, Iterable<IExportConfig> exports,
            LanguageIdentifier languageIdentifier) {
            super(context);
            this.origin = origin;
            this.exports = exports;
            this.languageIdentifier = languageIdentifier;
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputTransient<File>, ArchiveBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(ArchiveBuilder.class, Input.class);


    public ArchiveBuilder(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputTransient<File>, ArchiveBuilder, SpoofaxBuilderFactory<Input, OutputTransient<File>, ArchiveBuilder>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Archive language implementation";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("archive.dep");
    }

    @Override protected OutputTransient<File> build(Input input) throws Throwable {
        requireBuild(input.origin);

        final ZipArchiver zipArchiver = new ZipArchiver();
        final FileObject root = paths.root();

        require(toFile(paths.iconsDir()), new DirectoryModifiedStamper());
        zipArchiver.addFilesTo(root.getName(), paths.iconsDir(), FileSelectorUtils.all());

        require(toFile(paths.targetMetaborgDir()), new DirectoryModifiedStamper());
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
}
