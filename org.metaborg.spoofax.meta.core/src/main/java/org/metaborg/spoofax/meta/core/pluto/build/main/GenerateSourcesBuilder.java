package org.metaborg.spoofax.meta.core.pluto.build.main;

import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF_NAME;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.CopyJar;
import org.metaborg.spoofax.meta.core.pluto.build.MetaSdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Parenthesize;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg.Input;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.util.cmd.Arguments;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class GenerateSourcesBuilder extends SpoofaxBuilder<SpoofaxInput, None> {
    public static SpoofaxBuilderFactory<SpoofaxInput, None, GenerateSourcesBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(GenerateSourcesBuilder.class, SpoofaxInput.class);


    public GenerateSourcesBuilder(SpoofaxInput input) {
        super(input);
    }


    public static
        BuildRequest<SpoofaxInput, None, GenerateSourcesBuilder, SpoofaxBuilderFactory<SpoofaxInput, None, GenerateSourcesBuilder>>
        request(SpoofaxInput input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
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

        // SDF
        final String sdfModule = settings.sdfName();
        final String metaSdfModule = settings.metaSdfName();
        final Arguments sdfArgs = sdfArgs(context);

        requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, sdfModule, sdfArgs));
        requireBuild(MetaSdf2Table.factory, new MetaSdf2Table.Input(context, metaSdfModule, sdfArgs));

        // PP tables
        final File ppPackInputPath = toFile(settings.getPpFile(sdfModule));
        final File ppPackOutputPath = toFile(settings.getPpAfCompiledFile(sdfModule));
        requireBuild(PPGen.factory, new PPGen.Input(context, sdfModule, sdfArgs));
        requireBuild(PPPack.factory, new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, sdfModule, sdfArgs));

        // Parenthesizer
        final Origin sdf2Parenthesize =
            Sdf2Parenthesize.origin(new Sdf2Parenthesize.Input(context, sdfModule, sdfArgs));

        // Stratego signature
        final Origin rtg2Sig = Rtg2Sig.origin(new Rtg2Sig.Input(context, sdfModule, sdfArgs));

        // Stratego
        if(!context.isBuildStrategoEnabled(this)) {
            return None.val;
        }

        final File externalJar = settings.externalJar() != null ? new File(settings.externalJar()) : null;

        requireBuild(CopyJar.factory, new CopyJar.Input(context, externalJar));

        final File inputFile = toFile(context.settings.getStrMainFile());
        final File outputFile;
        final File depFile;
        if(context.settings.format() == Format.ctree) {
            outputFile = toFile(settings.getStrCompiledCtreeFile());
            depFile = outputFile;
        } else {
            outputFile = toFile(settings.getStrJavaMainFile());
            depFile = toFile(settings.getStrJavaTransDirectory());
        }
        final File cacheDir = toFile(settings.getCacheDirectory());

        final Iterable<FileObject> paths =
            context.languagePathService().sourceAndIncludePaths(context.project, SpoofaxConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for(FileObject path : paths) {
            final File file = toFileReplicate(path);
            includeDirs.add(file);
        }

        final Arguments strategoArgs = strategoArgs();

        // @formatter:off
        final Origin requiredUnits = Origin.Builder()
            .add(sdf2Parenthesize)
            .add(rtg2Sig)
            .get()
            ;
        // @formatter:on

        requireBuild(Strj.factory, new Strj.Input(context, inputFile, outputFile, depFile, "trans", true, true,
            Iterables.toArray(includeDirs, File.class), new String[0], cacheDir, strategoArgs, requiredUnits));

        return None.val;
    }


    public static Arguments sdfArgs(SpoofaxContext context) {
        final Arguments args = new Arguments();
        args.addAll(context.settings.sdfArgs());
        
        final Iterable<FileObject> paths =
            context.languagePathService().sourceAndIncludePaths(context.project, LANG_SDF_NAME);
        for(FileObject path : paths) {
            try {
                if(path.exists()) {
                    final File file = context.toFileReplicate(path);
                    if(path.getName().getExtension().equals("def")) {
                        args.addFile("-Idef", file);
                    } else {
                        args.addFile("-I", file);
                    }
                }
            } catch(FileSystemException e) {
                // Ignore path if path.exists fails.
            }
        }
        return args;
    }

    public Arguments strategoArgs() {
        final SpoofaxProjectSettings settings = context.settings;
        
        final Arguments args = new Arguments();
        args.addAll(context.settings.strategoArgs());

        if(settings.format() == Format.ctree) {
            args.add("-F");
        } else {
            args.addAll("-la", "java-front");
            if(context.isJavaJarEnabled(this)) {
                args.addAll("-la", settings.strategiesPackageName());
            }
        }

        final String externalJarFlags = settings.externalJarFlags();
        if(externalJarFlags != null) {
            args.addLine(externalJarFlags);
        }

        return args;
    }
}
