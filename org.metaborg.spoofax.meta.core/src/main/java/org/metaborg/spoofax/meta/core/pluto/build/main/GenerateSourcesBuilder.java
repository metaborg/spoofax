package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.CopyJar;
import org.metaborg.spoofax.meta.core.pluto.build.MetaSdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Parenthesize;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig.Input;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.output.None;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class GenerateSourcesBuilder extends SpoofaxBuilder<SpoofaxInput, None> {
    public static SpoofaxBuilderFactory<SpoofaxInput, None, GenerateSourcesBuilder> factory = SpoofaxBuilderFactoryFactory.of(
        GenerateSourcesBuilder.class, SpoofaxInput.class);


    public GenerateSourcesBuilder(SpoofaxInput input) {
        super(input);
    }


    @Override protected String description(SpoofaxInput input) {
        return "Generate sources";
    }

    @Override public File persistentPath(SpoofaxInput input) {
        return context.depPath("generate-sources.dep");
    }

    @Override public None build(SpoofaxInput input) throws IOException {
        // TODO: build programs of languages with Pluto.
        // requireBuild(CompileSpoofaxPrograms.factory, new CompileSpoofaxPrograms.Input(context));

        final SpoofaxProjectSettings settings = context.settings;

        final String sdfModule = settings.sdfName();
        //final String strModule = settings.strategoName(); // using context.settings.getStrMainFile()
        final String metaSdfModule = settings.metaSdfName();
        final String sdfArgs = Joiner.on(' ').join(settings.sdfArgs());
        //final File externalDef = settings.externalDef() != null ? new File(settings.externalDef()) : null;
        final File externalJar = settings.externalJar() != null ? new File(settings.externalJar()) : null;
        final String externalJarFlags = settings.externalJarFlags();

        // SDF
        requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, sdfModule, sdfArgs));
        requireBuild(MetaSdf2Table.factory, new MetaSdf2Table.Input(context, metaSdfModule));

        // PP tables
        final File ppPackInputPath = FileUtils.toFile(settings.getPpFile(sdfModule));
        final File ppPackOutputPath = FileUtils.toFile(settings.getPpAfCompiledFile(sdfModule));
        requireBuild(PPGen.factory, new PPGen.Input(context, sdfModule));
        requireBuild(PPPack.factory, new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, true));

        // Parenthesizer
        final BuildRequest<Sdf2Parenthesize.Input, None, Sdf2Parenthesize, ?> sdf2Parenthesize =
            new BuildRequest<>(Sdf2Parenthesize.factory, new Sdf2Parenthesize.Input(context, sdfModule));

        // Stratego signature
        final BuildRequest<Rtg2Sig.Input, None, Rtg2Sig, ?> rtg2Sig =
            new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, sdfModule));

        // Stratego
        if(!context.isBuildStrategoEnabled(this)) {
            return None.val;
        }

        requireBuild(CopyJar.factory, new CopyJar.Input(context, externalJar));

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
        if(externalJarFlags != null) {
            Collections.addAll(extraArgs, externalJarFlags.split("[\\s]+"));
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
            ArrayUtils.arrayAdd(rtg2Sig, new BuildRequest<?, ?, ?, ?>[] { rtg2Sig, sdf2Parenthesize })));

        // BuildRequest<PrepareStrj.Input, OutputPersisted<File>, PrepareStrj, ?> strategoCtree =
        // new BuildRequest<>(PrepareStrj.factory, new PrepareStrj.Input(context, sdfModule, sdfArgs, strModule,
        // externalJar, externalJarFlags, externalDef, new BuildRequest<?, ?, ?, ?>[] { sdf2Parenthesize }));
        // requireBuild(strategoCtree);

        // TODO: run Java compilation and packaging as a single Pluto build.

        return None.val;
    }
}
