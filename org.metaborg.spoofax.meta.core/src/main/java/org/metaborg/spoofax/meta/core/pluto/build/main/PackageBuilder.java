package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.*;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.misc.Copy;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryLastModifiedStamper;
import org.metaborg.util.cmd.Arguments;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

import com.google.common.collect.Lists;

public class PackageBuilder extends SpoofaxBuilder<PackageBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File strategoMainFile;
        public final File strategoJavaStrategiesMainFile;
        public final File baseDir;
        public final Arguments sdfArgs;
        public final StrategoFormat format;
        public final Collection<File> javaJarPaths;
        public final File javaJarOutput;
        public final String sdfName;
        public final File jarTarget;
        public final File jarOutput;
        public final File targetPpAfFile;
        public final File targetGenPpAfFile;
        public final File targetTblFile;
        public final File ppPackInputPath;
        public final File ppPackOutputPath;
        public final File ppGenInputPath;
        public final File ppGenOutputPath;
        public final File afGenOutputPath;
        @Nullable public final File externalDef;
        public final File packSdfInputPath;
        public final File packSdfOutputPath;
        public final File syntaxFolder;
        public final File genSyntaxFolder;
        public final File makePermissiveOutputPath;
        public final File sdf2tableOutputPath;

        public Input(SpoofaxContext context, File strategoMainFile, File strategoJavaStrategiesMainFile,
                     Arguments sdfArgs, File baseDir, StrategoFormat format, Collection<File> javaJarPaths, File javaJarOutput,
            String sdfName, File jarTarget, File jarOutput, File targetPpAfFile, File targetGenPpAfFile,
            File targetTblFile, File ppPackInputPath, File ppPackOutputPath, File ppGenInputPath, File ppGenOutputPath,
            File afGenOutputPath, File makePermissiveOutputPath, File sdf2tableOutputPath, @Nullable File externalDef,
            File packSdfInputPath, File packSdfOutputPath, File syntaxFolder, File genSyntaxFolder) {
            super(context);
            this.strategoMainFile = strategoMainFile;
            this.strategoJavaStrategiesMainFile = strategoJavaStrategiesMainFile;
            this.sdfArgs = sdfArgs;
            this.baseDir = baseDir;
            this.format = format;
            this.javaJarPaths = javaJarPaths;
            this.javaJarOutput = javaJarOutput;
            this.sdfName = sdfName;
            this.jarTarget = jarTarget;
            this.jarOutput = jarOutput;
            this.targetPpAfFile = targetPpAfFile;
            this.targetGenPpAfFile = targetGenPpAfFile;
            this.targetTblFile = targetTblFile;
            this.ppPackInputPath = ppPackInputPath;
            this.ppPackOutputPath = ppPackOutputPath;
            this.ppGenInputPath = ppGenInputPath;
            this.ppGenOutputPath = ppGenOutputPath;
            this.afGenOutputPath = afGenOutputPath;
            this.makePermissiveOutputPath = makePermissiveOutputPath;
            this.sdf2tableOutputPath = sdf2tableOutputPath;
            this.externalDef = externalDef;
            this.packSdfInputPath = packSdfInputPath;
            this.packSdfOutputPath = packSdfOutputPath;
            this.syntaxFolder = syntaxFolder;
            this.genSyntaxFolder = genSyntaxFolder;
        }
    }

    public static SpoofaxBuilderFactory<Input, None, PackageBuilder> factory = SpoofaxBuilderFactoryFactory.of(
        PackageBuilder.class, Input.class);


    public PackageBuilder(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, PackageBuilder, SpoofaxBuilderFactory<Input, None, PackageBuilder>>
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

    @Override protected None build(Input input) throws Throwable {
        // TODO: build Java code with Pluto.
        // BuildRequest<CompileJavaCode.Input, None, CompileJavaCode, ?> compileJavaCode =
        // new BuildRequest<>(CompileJavaCode.factory, new CompileJavaCode.Input(context,
        // new BuildRequest<?, ?, ?, ?>[] { strategoCtree }));
        // requireBuild(compileJavaCode);

        if(context.isJavaJarEnabled(this, input.strategoJavaStrategiesMainFile)) {
            jar(input.javaJarOutput, input.baseDir, null, input.javaJarPaths);
        }

        if(input.format == StrategoFormat.jar) {
            // Copy .pp.af and .tbl to trans, so that they get included in the JAR file. Required for being able
            // to load those files from Stratego code.
            // TODO: extract build request/origin creation for these files into separate class to prevent code dup.
            final String sdfModule = input.sdfName;
            final Arguments sdfArgs = GenerateSourcesBuilder.sdfArgs(context, input.sdfArgs);
            final PackSdf.Input packSdfInput =
                GenerateSourcesBuilder.packSdfInput(context, sdfModule, sdfArgs, input.externalDef,
                    input.packSdfInputPath, input.packSdfOutputPath, input.syntaxFolder, input.genSyntaxFolder);
            final Origin packSdf = PackSdf.origin(packSdfInput);

            final Origin.Builder originBuilder = Origin.Builder();

            final PPPack.Input ppPackInput =
                GenerateSourcesBuilder.ppPackInput(context, input.ppPackInputPath, input.ppPackOutputPath, packSdf);
            final File ppAfFile = requireBuild(PPPack.factory, ppPackInput).val;
            originBuilder.add(Copy.origin(new Copy.Input(ppAfFile, input.targetPpAfFile, Origin.from(PPPack
                .request(ppPackInput)), context.baseDir, context.depDir)));

            final PPGen.Input ppGenInput =
                GenerateSourcesBuilder.ppGenInput(context, input.ppGenInputPath, input.ppGenOutputPath,
                    input.afGenOutputPath, sdfModule, packSdf);
            final File ppGenFile = requireBuild(PPGen.factory, ppGenInput).val;
            originBuilder.add(Copy.origin(new Copy.Input(ppGenFile, input.targetGenPpAfFile, Origin.from(PPGen
                .request(ppGenInput)), context.baseDir, context.depDir)));

            final Sdf2Table.Input sdf2TableInput =
                GenerateSourcesBuilder.sdf2TableInput(context, input.sdf2tableOutputPath,
                    input.makePermissiveOutputPath, sdfModule, packSdfInput);
            final File tblFile = requireBuild(Sdf2Table.factory, sdf2TableInput).val;
            originBuilder.add(Copy.origin(new Copy.Input(tblFile, input.targetTblFile, Origin.from(Sdf2Table
                .request(sdf2TableInput)), context.baseDir, context.depDir)));

            final Origin origin = originBuilder.get();
            requireBuild(origin);

            // Jar
            jar(input.jarOutput, input.baseDir, origin, input.jarTarget);
        }

        return None.val;
    }

    public void jar(File jarFile, File baseDir, @Nullable Origin origin, File... paths) throws IOException {
        jar(jarFile, baseDir, origin, Lists.newArrayList(paths));
    }

    public void jar(File jarFile, File baseDir, @Nullable Origin origin, Collection<File> paths) throws IOException {
        final Collection<JarBuilder.Entry> fileEntries = Lists.newLinkedList();

        for(File path : paths) {
            require(path, new DirectoryLastModifiedStamper());
            final Collection<File> files = findFiles(path);
            for(final File javaFile : files) {
                final String relative = relativize(javaFile, baseDir);
                // Convert \ to / on Windows; ZIP/JAR files must use / for paths.
                // HACK: this should be fixed in the JarBuilder.
                final String forwardslashRelative = relative.replace('\\', '/');
                // Ignore files that are not relative to the base directory.
                if(relative != null) {
                    fileEntries.add(new JarBuilder.Entry(forwardslashRelative, javaFile));
                }
            }
        }

        requireBuild(JarBuilder.factory, new JarBuilder.Input(jarFile, fileEntries, origin));
    }

    private @Nullable String relativize(File path, File base) {
        @Nullable String relative = FilenameUtils.normalize(base.toPath().relativize(path.toPath()).toString());
        if(relative == null || relative.equals(""))
            return null;
        return relative;
    }

    private Collection<File> findFiles(File directory) {
        if(!directory.isDirectory())
            return Collections.emptyList();
        return FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    }

}
