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
import org.metaborg.spoofax.meta.core.pluto.build.MakePermissive;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Parenthesize;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;

public class GenerateSourcesBuilder extends SpoofaxBuilder<GenerateSourcesBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final String sdfName;
        public final String metaSdfName;
        public final @Nullable File externalJar;
        public final @Nullable File strjTarget;
        public final File strjInputFile;
        public final File strjOutputFile;
        public final File strjDepFile;
        public final File strjCacheDir;
        public final Format format;
        public final String strategiesPackageName;
        public final String externalJarFlags;

        public Input(SpoofaxContext context, String sdfName, String metaSdfName, File externalJar, File strjTarget, File strjInputFile, File strjOutputFile, File strjDepFile, File strjCacheDir, Format format, String strategiesPackageName, String externalJarFlags) {
            super(context);
            this.sdfName = sdfName;
            this.metaSdfName = metaSdfName;
            this.externalJar = externalJar;
            this.strjTarget = strjTarget;
            this.strjInputFile = strjInputFile;
            this.strjOutputFile = strjOutputFile;
            this.strjDepFile = strjDepFile;
            this.strjCacheDir = strjCacheDir;
            this.format = format;
            this.strategiesPackageName = strategiesPackageName;
            this.externalJarFlags = externalJarFlags;
        }
    }

    public static SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder> factory = SpoofaxBuilderFactoryFactory.of(
        GenerateSourcesBuilder.class, Input.class);


    public GenerateSourcesBuilder(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, None, GenerateSourcesBuilder, SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Generate sources";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("generate-sources.dep");
    }

    @Override public None build(Input input) throws IOException {
        final SpoofaxProjectSettings settings = context.settings;

        // SDF
        final String sdfModule = input.sdfName;
        final Arguments sdfArgs = sdfArgs(context);

        final PackSdf.Input packSdfInput = packSdfInput(context, sdfModule, sdfArgs);
        final Origin packSdfOrigin = PackSdf.origin(packSdfInput);


        sdf2Table(sdfModule, input.sdfName, packSdfInput);
        metaSdf2Table(input.sdfName, input.metaSdfName, sdfArgs);
        ppGen(sdfModule, packSdfOrigin);
        ppPack(sdfModule, packSdfOrigin);
        final Origin sdf2Parenthesize = sdf2Parenthesize(sdf2ParenthesizeInput(context, sdfModule, packSdfOrigin));
//        final Origin sdf2Parenthesize = sdf2Parenthesize(settings, sdfModule, packSdfOrigin);
        final Sdf2Rtg.Input sdf2RtgInput = sdf2Rtg(settings, sdfModule, packSdfOrigin);
        final Origin rtg2Sig = rtg2Sig(settings, sdfModule, sdf2RtgInput);
//        final Origin rtg2Sig = rtg2Sig(settings, sdfModule, input.sdf2RtgInput);

        // Stratego
        if(!context.isBuildStrategoEnabled(this)) {
            return None.val;
        }

        // @formatter:off
        final Origin requiredUnits = Origin.Builder()
            .add(sdf2Parenthesize)
            .add(rtg2Sig)
            .get()
            ;
        // @formatter:on
        strj(input, requiredUnits);
//        strj(settings, requiredUnits);

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

    public static PackSdf.Input packSdfInput(SpoofaxContext context, String module, Arguments args) {
        final SpoofaxProjectSettings settings = context.settings;
        final File externalDef;
        if(settings.externalDef() != null) {
            externalDef = context.toFile(context.resourceService().resolve(settings.externalDef()));
        } else {
            externalDef = null;
        }
        final File packSdfInputPath = context.toFile(settings.getSdfMainFile(module));
        final File packSdfOutputPath = context.toFile(settings.getSdfCompiledDefFile(module));
        final File syntaxFolder = context.toFile(settings.getSyntaxDirectory());
        final File genSyntaxFolder = context.toFile(settings.getGenSyntaxDirectory());

        return new PackSdf.Input(context, module, args, externalDef, packSdfInputPath, packSdfOutputPath, syntaxFolder, genSyntaxFolder);
    }

    public static Sdf2Table.Input sdf2TableInput(SpoofaxContext context, String module, String sdfName, PackSdf.Input packSdfInput) {
        final SpoofaxProjectSettings settings = context.settings;
        final File makePermissiveOutputPath = context.toFile(settings.getSdfCompiledPermissiveDefFile(module));
        final String depFilename = "make-permissive." + sdfName + ".dep";
        final MakePermissive.Input makePermissiveInput =
            new MakePermissive.Input(context, makePermissiveOutputPath, depFilename, packSdfInput);

        final File sdf2tableOutputPath = context.toFile(settings.getSdfCompiledTableFile(module));
        final Sdf2Table.Input sdf2TableInput = new Sdf2Table.Input(context, sdf2tableOutputPath, makePermissiveInput);
        return sdf2TableInput;
    }

    private void sdf2Table(String sdfModule, String sdfName, PackSdf.Input packSdfInput) throws IOException {
        final Sdf2Table.Input input = sdf2TableInput(context, sdfModule, sdfName, packSdfInput);
        requireBuild(Sdf2Table.factory, input);
    }

    private void metaSdf2Table(String sdfName, String metaSdfName, Arguments sdfArgs) throws IOException {
        final String module = metaSdfName;
        final Arguments args = new Arguments(sdfArgs);
        final PackSdf.Input packInput = packSdfInput(context, module, args);
        final Sdf2Table.Input tableInput = sdf2TableInput(context, module, sdfName, packInput);
        require(tableInput.inputModule(), SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        if(FileCommands.exists(tableInput.inputModule())) {
            final FileObject strategoMixPath = context.resourceService().resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = toFileReplicate(strategoMixPath);
            tableInput.sdfArgs().addFile("-Idef", strategoMixFile);
            provide(strategoMixFile);
            requireBuild(Sdf2Table.factory, tableInput);
        }
    }

    public static PPGen.Input ppGenInput(SpoofaxContext context, String module, Origin origin) {
        final SpoofaxProjectSettings settings = context.settings;
        final File inputPath = context.toFile(settings.getSdfCompiledDefFile(module));
        final File ppOutputPath = context.toFile(settings.getGenPpCompiledFile(module));
        final File afOutputPath = context.toFile(settings.getGenPpAfCompiledFile(module));
        return new PPGen.Input(context, inputPath, ppOutputPath, afOutputPath, module, origin);
    }

    private void ppGen(String module, Origin origin) throws IOException {
        requireBuild(PPGen.factory, ppGenInput(context, module, origin));
    }

    public static PPPack.Input ppPackInput(SpoofaxContext context, String module, Origin origin) {
        final SpoofaxProjectSettings settings = context.settings;
        final File inputPath = context.toFile(settings.getPpFile(module));
        final File outputPath = context.toFile(settings.getPpAfCompiledFile(module));
        return new PPPack.Input(context, inputPath, outputPath, origin);
    }

    private void ppPack(String module, Origin origin) throws IOException {
        requireBuild(PPPack.factory, ppPackInput(context, module, origin));
    }

    public static Sdf2Parenthesize.Input sdf2ParenthesizeInput(SpoofaxContext context, String module, Origin origin) {
        final SpoofaxProjectSettings settings = context.settings;
        final File inputPath = context.toFile(settings.getSdfCompiledDefFile(module));
        final File outputPath = context.toFile(settings.getStrCompiledParenthesizerFile(module));
        final String outputModule = "include/" + module + "-parenthesize";
        return new Sdf2Parenthesize.Input(context, inputPath, outputPath, outputModule, module,
                origin);
    }

    private Origin sdf2Parenthesize(Sdf2Parenthesize.Input input) {
        return Sdf2Parenthesize.origin(input);
    }

    private Sdf2Rtg.Input sdf2Rtg(SpoofaxProjectSettings settings, String module, Origin origin) {
        final File inputPath = toFile(settings.getSdfCompiledDefFile(module));
        final File outputPath = toFile(settings.getRtgFile(module));
        return new Sdf2Rtg.Input(context, inputPath, outputPath, module, origin);
    }

    private Origin rtg2Sig(final SpoofaxProjectSettings settings, final String module, final Sdf2Rtg.Input input) {
        final File outputPath = toFile(settings.getStrCompiledSigFile(module));
        return Rtg2Sig.origin(new Rtg2Sig.Input(context, outputPath, input));
    }


    private void strj(
            Input input,
            Origin origin) throws IOException {
//        final File externalJar;
//        final File target;
//        final String externalJarFilename = settings.externalJar();
//        if (externalJarFilename != null) {
//            externalJar = new File(externalJarFilename);
//            target = toFile(settings.getIncludeDirectory().resolveFile(externalJar.getName()));
//        } else {
//            externalJar = null;
//            target = null;
//        }

        requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externalJar, input.strjTarget));

//        final File inputFile = toFile(context.settings.getStrMainFile());
//        final File outputFile;
//        final File depFile;
//        if(context.settings.format() == Format.ctree) {
//        if(format == Format.ctree) {
//            outputFile = toFile(settings.getStrCompiledCtreeFile());
//            depFile = outputFile;
//        } else {
//            outputFile = toFile(settings.getStrJavaMainFile());
//            depFile = toFile(settings.getStrJavaTransDirectory());
//        }
//        final File cacheDir = toFile(settings.getCacheDirectory());

        final Iterable<FileObject> paths =
            context.languagePathService().sourceAndIncludePaths(context.project, SpoofaxConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for(FileObject path : paths) {
            final File file = toFileReplicate(path);
            includeDirs.add(file);
        }

        final Arguments strategoArgs = strategoArgs(input.format, input.strategiesPackageName, input.externalJarFlags);
//        final Arguments strategoArgs = strategoArgs(settings.format(), settings.strategiesPackageName(), settings.externalJarFlags());

        requireBuild(Strj.factory, new Strj.Input(context, input.strjInputFile, input.strjOutputFile, input.strjDepFile, "trans", true, true,
            Iterables.toArray(includeDirs, File.class), new String[0], input.strjCacheDir, strategoArgs, origin));
    }


    public Arguments strategoArgs(Format format, String strategiesPackageName, @Nullable String externalJarFlags) {
        final Arguments args = new Arguments();
        args.addAll(context.settings.strategoArgs());

        if(format == Format.ctree) {
            args.add("-F");
        } else {
            args.addAll("-la", "java-front");
            if(context.isJavaJarEnabled(this)) {
                args.addAll("-la", strategiesPackageName);
            }
        }

        if(externalJarFlags != null) {
            args.addLine(externalJarFlags);
        }

        return args;
    }
}
