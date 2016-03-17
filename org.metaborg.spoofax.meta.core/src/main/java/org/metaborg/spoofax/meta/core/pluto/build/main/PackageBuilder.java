package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.misc.CopyPattern;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryLastModifiedStamper;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class PackageBuilder extends SpoofaxMainBuilder<PackageBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final GenerateSourcesBuilder.Input generateSourcesInput;

        // Shared
        public final File classesDir;

        // Stratego JAR
        public final StrategoFormat format;
        public final File strategoJavaSourceDir;
        public final File strategoJarInputDir;
        public final File strategoJarOutput;

        // Stratego Java-strategies JAR
        public final File strategiesMainFile;
        public final Collection<File> strategiesJarInputs;
        public final File strategiesJarOutput;


        public Input(SpoofaxContext context, GenerateSourcesBuilder.Input generateSourcesInput, File classesDir,
            StrategoFormat format, File strategoJavaSourceDir, File strategoJarInputDir, File strategoJarOutput,
            File strategiesMainFile, Collection<File> strategiesJarInputs, File strategiesJarOutput) {
            super(context);

            this.generateSourcesInput = generateSourcesInput;

            this.classesDir = classesDir;

            this.format = format;
            this.strategoJavaSourceDir = strategoJavaSourceDir;
            this.strategoJarInputDir = strategoJarInputDir;
            this.strategoJarOutput = strategoJarOutput;

            this.strategiesMainFile = strategiesMainFile;
            this.strategiesJarInputs = strategiesJarInputs;
            this.strategiesJarOutput = strategiesJarOutput;
        }
    }

    public static SpoofaxBuilderFactory<Input, None, PackageBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(PackageBuilder.class, Input.class);


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
        if(input.format == StrategoFormat.jar) {
            // Get all required origins for sound incrementality.
            final Arguments sdfArgs = sdfArgs(context, input.generateSourcesInput.sdfArgs);
            final PackSdf.Input packSdfInput = packSdfInput(context, input.generateSourcesInput.sdfName, sdfArgs,
                input.generateSourcesInput.externalDef, input.generateSourcesInput.packSdfInputPath,
                input.generateSourcesInput.packSdfOutputPath, input.generateSourcesInput.syntaxFolder,
                input.generateSourcesInput.genSyntaxFolder);
            final Origin packSdf = PackSdf.origin(packSdfInput);

            final Origin.Builder originBuilder = Origin.Builder();

            final PPPack.Input ppPackInput = ppPackInput(context, input.generateSourcesInput.ppPackInputPath,
                input.generateSourcesInput.ppPackOutputPath, packSdf);
            originBuilder.add(PPPack.origin(ppPackInput));

            final PPGen.Input ppGenInput = ppGenInput(context, input.generateSourcesInput.ppGenInputPath,
                input.generateSourcesInput.ppGenOutputPath, input.generateSourcesInput.afGenOutputPath,
                input.generateSourcesInput.sdfName, packSdf);
            originBuilder.add(PPGen.origin(ppGenInput));

            final Sdf2Table.Input sdf2TableInput = sdf2TableInput(context,
                input.generateSourcesInput.sdf2tableOutputPath, input.generateSourcesInput.makePermissiveOutputPath,
                input.generateSourcesInput.sdfName, packSdfInput);
            originBuilder.add(Sdf2Table.origin(sdf2TableInput));

            final Origin sdf2Parenthesize =
                sdf2Parenthesize(sdf2ParenthesizeInput(context, input.generateSourcesInput, packSdf));
            final Sdf2Rtg.Input sdf2RtgInput = sdf2Rtg(context, input.generateSourcesInput, packSdf);
            final Origin rtg2Sig = rtg2Sig(rtg2SigInput(context, input.generateSourcesInput, sdf2RtgInput));
            final Origin strj = strj(input.generateSourcesInput, sdf2Parenthesize, rtg2Sig);
            originBuilder.add(strj);

            final Origin origin = originBuilder.get();


            // Copy .pp.af and .tbl to JAR target directory, so that they get included in the JAR file.
            // Required for being able to import-term those files from Stratego code.
            final CopyPattern.Input copyPatternInput = new CopyPattern.Input(input.strategoJavaSourceDir,
                input.strategoJarInputDir, ".+\\.(?:tbl|pp\\.af)", origin, context.baseDir, context.depDir);
            final Origin copyPatternOrigin = CopyPattern.origin(copyPatternInput);
            requireBuild(copyPatternOrigin);

            jar(input.strategoJarOutput, input.classesDir, copyPatternOrigin, input.strategoJarInputDir);
        }

        if(context.isJavaJarEnabled(this, input.strategiesMainFile)) {
            jar(input.strategiesJarOutput, input.classesDir, null, input.strategiesJarInputs);
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
                // Ignore files that are not relative to the base directory.
                if(relative != null) {
                    final String forwardslashRelative = relative.replace('\\', '/');
                    fileEntries.add(new JarBuilder.Entry(forwardslashRelative, javaFile));
                }
            }
        }

        requireBuild(JarBuilder.factory, new JarBuilder.Input(jarFile, fileEntries, origin));
    }

    private @Nullable String relativize(File path, File base) {
        final String relative = FilenameUtils.normalize(base.toPath().relativize(path.toPath()).toString());
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
