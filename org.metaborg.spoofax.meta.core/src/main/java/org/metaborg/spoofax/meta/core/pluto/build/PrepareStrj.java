package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PrepareStrj extends SpoofaxBuilder<PrepareStrj.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 6323245405121428720L;

        public final String sdfModule;
        public final String buildSdfImports;
        public final String strModule;
        public final File externalJar;
        public final String externalJarflags;
        public final File externalDef;

        public final BuildRequest<?, ?, ?, ?>[] requiredUnits;


        public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, String strmodule,
            File externaljar, String externaljarflags, File externalDef, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
            super(context);
            this.sdfModule = sdfmodule;
            this.buildSdfImports = buildSdfImports;
            this.strModule = strmodule;
            this.externalJar = externaljar;
            this.externalJarflags = externaljarflags;
            this.externalDef = externalDef;
            this.requiredUnits = requiredUnits;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, PrepareStrj> factory =
        SpoofaxBuilderFactoryFactory.of(PrepareStrj.class, Input.class);


    public PrepareStrj(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Prepare Stratego code";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("prepare-strj." + input.sdfModule + "." + input.strModule + ".dep");
    }

    @Override public Stamper defaultStamper() {
        return LastModifiedStamper.instance;
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        BuildRequest<Rtg2Sig.Input, None, Rtg2Sig, ?> rtg2Sig =
            new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfModule));

        if(!context.isBuildStrategoEnabled(this)) {
            final String strategoModule = context.settings.strategoName();
            throw new IllegalArgumentException(String.format("Main stratego file '%s' not found", strategoModule));
        }

        requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externalJar));

        final File inputPath = FileUtils.toFile(context.settings.getStrMainFile());
        final File outputPath;
        final File depPath;
        if(context.settings.format() == Format.ctree) {
            outputPath = FileUtils.toFile(context.settings.getStrCompiledCtreeFile());
            depPath = outputPath;
        } else {
            outputPath = FileUtils.toFile(context.settings.getStrJavaMainFile());
            depPath = FileUtils.toFile(context.settings.getStrJavaTransDirectory());
        }
        final File cacheDir = FileUtils.toFile(context.settings.getCacheDirectory());

        final Collection<String> extraArgs = Lists.newLinkedList();
        if(context.settings.format() == Format.ctree) {
            extraArgs.add("-F");
        } else {
            extraArgs.add("-la java-front");
            if(context.isJavaJarEnabled(this)) {
                extraArgs.add("-la " + context.settings.strategiesPackageName());
            }
        }
        if(input.externalJarflags != null) {
            Collections.addAll(extraArgs, input.externalJarflags.split("[\\s]+"));
        }

        final Iterable<FileObject> paths =
            context.languagePathService.sourceAndIncludePaths(context.project, SpoofaxConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for(FileObject path : paths) {
            File file = context.resourceService.localFile(path);
            includeDirs.add(file);
        }

        // TODO: get libraries from stratego arguments
        requireBuild(Strj.factory, new Strj.Input(context, inputPath, outputPath, depPath, "trans", true, true,
            Iterables.toArray(includeDirs, File.class), new String[] { "stratego-lib", "stratego-sglr", "stratego-gpp",
                "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc" }, cacheDir, extraArgs.toArray(new String[0]),
            ArrayUtils.arrayAdd(rtg2Sig, input.requiredUnits)));

        return OutputPersisted.of(outputPath);
    }
}
