package org.metaborg.spoofax.meta.core.pluto.build.main;

import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF_NAME;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
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

        public final File strategoMainFile;
        public final File strategoJavaStrategiesMainFile;
        public final String sdfName;
        public final String metaSdfName;
        public final Arguments sdfArgs;
        @Nullable
        public final File externalJar;
        @Nullable
        public final File strjTarget;
        public final File strjInputFile;
        public final File strjOutputFile;
        public final File strjDepFile;
        public final File strjCacheDir;
        public final Arguments strategoArgs;
        public final StrategoFormat format;
        public final String strategiesPackageName;
        public final String externalJarFlags;
        public final File rtg2SigOutputFile;
        public final File sdf2RtgInputFile;
        public final File sdf2RtgOutputFile;
        public final File sdf2ParenthesizeInputFile;
        public final File sdf2ParenthesizeOutputFile;
        public final String sdf2ParenthesizeOutputModule;
        public final File ppPackInputPath;
        public final File ppPackOutputPath;
        public final File ppGenInputPath;
        public final File ppGenOutputPath;
        public final File afGenOutputPath;
        @Nullable
        public final File externalDef;
        public final File packSdfInputPath;
        public final File packSdfOutputPath;
        public final File packMetaSdfInputPath;
        public final File packMetaSdfOutputPath;
        public final File syntaxFolder;
        public final File genSyntaxFolder;
        public final File makePermissiveOutputPath;
        public final File sdf2tableOutputPath;

        public Input(SpoofaxContext context,
                     File strategoMainFile,
                     File strategoJavaStrategiesMainFile,
                     String sdfName,
                     String metaSdfName,
                     Arguments sdfArgs,
                     @Nullable File externalJar,
                     @Nullable File strjTarget,
                     File strjInputFile,
                     File strjOutputFile,
                     File strjDepFile,
                     File strjCacheDir,
                     Arguments strategoArgs,
                     StrategoFormat format,
                     String strategiesPackageName,
                     String externalJarFlags,
                     File rtg2SigOutputFile,
                     File sdf2RtgInputFile,
                     File sdf2RtgOutputFile,
                     File sdf2ParenthesizeInputFile,
                     File sdf2ParenthesizeOutputFile,
                     String sdf2ParenthesizeOutputModule,
                     File ppPackInputPath,
                     File ppPackOutputPath,
                     File ppGenInputPath,
                     File ppGenOutputPath,
                     File afGenOutputPath,
                     File makePermissiveOutputPath,
                     File sdf2tableOutputPath,
                     @Nullable File externalDef,
                     File packSdfInputPath,
                     File packSdfOutputPath,
                     File packMetaSdfInputPath,
                     File packMetaSdfOutputPath,
                     File syntaxFolder,
                     File genSyntaxFolder) {
            super(context);
            this.strategoMainFile = strategoMainFile;
            this.strategoJavaStrategiesMainFile = strategoJavaStrategiesMainFile;
            this.sdfName = sdfName;
            this.metaSdfName = metaSdfName;
            this.sdfArgs = sdfArgs;
            this.externalJar = externalJar;
            this.strjTarget = strjTarget;
            this.strjInputFile = strjInputFile;
            this.strjOutputFile = strjOutputFile;
            this.strjDepFile = strjDepFile;
            this.strjCacheDir = strjCacheDir;
            this.strategoArgs = strategoArgs;
            this.format = format;
            this.strategiesPackageName = strategiesPackageName;
            this.externalJarFlags = externalJarFlags;
            this.rtg2SigOutputFile = rtg2SigOutputFile;
            this.sdf2RtgInputFile = sdf2RtgInputFile;
            this.sdf2RtgOutputFile = sdf2RtgOutputFile;
            this.sdf2ParenthesizeInputFile = sdf2ParenthesizeInputFile;
            this.sdf2ParenthesizeOutputFile = sdf2ParenthesizeOutputFile;
            this.sdf2ParenthesizeOutputModule = sdf2ParenthesizeOutputModule;
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
            this.packMetaSdfInputPath = packMetaSdfInputPath;
            this.packMetaSdfOutputPath = packMetaSdfOutputPath;
            this.syntaxFolder = syntaxFolder;
            this.genSyntaxFolder = genSyntaxFolder;
        }
    }

    public static SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder> factory = SpoofaxBuilderFactoryFactory.of(
            GenerateSourcesBuilder.class, Input.class);


    public GenerateSourcesBuilder(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, GenerateSourcesBuilder, SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder>>
    request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override
    protected String description(Input input) {
        return "Generate sources";
    }

    @Override
    public File persistentPath(Input input) {
        return context.depPath("generate-sources.dep");
    }

    @Override
    public None build(Input input) throws IOException {
        // SDF
        final Arguments sdfArgs = sdfArgs(context, input.sdfArgs);

        final PackSdf.Input packSdfInput = packSdfInput(context, input.sdfName, sdfArgs, input.externalDef, input.packSdfInputPath, input.packSdfOutputPath, input.syntaxFolder, input.genSyntaxFolder);
        final Origin packSdfOrigin = PackSdf.origin(packSdfInput);
        final PackSdf.Input metaPackSdfInput = packSdfInput(context, input.metaSdfName, new Arguments(sdfArgs), input.externalDef, input.packMetaSdfInputPath, input.packMetaSdfOutputPath, input.syntaxFolder, input.genSyntaxFolder);

        sdf2Table(sdf2TableInput(context, input.sdf2tableOutputPath, input.makePermissiveOutputPath, input.sdfName, packSdfInput));
        metaSdf2Table(sdf2TableInput(context, input.sdf2tableOutputPath, input.makePermissiveOutputPath, input.metaSdfName, metaPackSdfInput));
        ppGen(ppGenInput(context, input.ppGenInputPath, input.ppGenOutputPath, input.afGenOutputPath, input.sdfName, packSdfOrigin));
        ppPack(ppPackInput(context, input.ppPackInputPath, input.ppPackOutputPath, packSdfOrigin));
        final Origin sdf2Parenthesize = sdf2Parenthesize(sdf2ParenthesizeInput(context, input, packSdfOrigin));
        final Sdf2Rtg.Input sdf2RtgInput = sdf2Rtg(context, input, packSdfOrigin);
        final Origin rtg2Sig = rtg2Sig(rtg2SigInput(context, input, sdf2RtgInput));

        // Stratego
        if (!context.isBuildStrategoEnabled(this, input.strategoMainFile)) {
            return None.val;
        }

        // @formatter:off
        final Origin requiredUnits = Origin.Builder()
                .add(sdf2Parenthesize)
                .add(rtg2Sig)
                .get();
        // @formatter:on
        strj(input, requiredUnits);

        return None.val;
    }


    public static Arguments sdfArgs(SpoofaxContext context, Arguments sdfArgs) {
        final Arguments args = new Arguments();
        args.addAll(sdfArgs);

        final Iterable<FileObject> paths =
                context.languagePathService().sourceAndIncludePaths(context.languageSpec, LANG_SDF_NAME);
        for (FileObject path : paths) {
            try {
                if (path.exists()) {
                    final File file = context.toFileReplicate(path);
                    if (path.getName().getExtension().equals("def")) {
                        args.addFile("-Idef", file);
                    } else {
                        args.addFile("-I", file);
                    }
                }
            } catch (FileSystemException e) {
                // Ignore path if path.exists fails.
            }
        }
        return args;
    }

    public static PackSdf.Input packSdfInput(SpoofaxContext context, String module, Arguments args, File externalDef, File packSdfInputPath, File packSdfOutputPath, File syntaxFolder, File genSyntaxFolder) {
        return new PackSdf.Input(context, module, args, externalDef, packSdfInputPath, packSdfOutputPath, syntaxFolder, genSyntaxFolder);
    }

    public static Sdf2Table.Input sdf2TableInput(SpoofaxContext context, File sdf2tableOutputPath, File makePermissiveOutputPath, String sdfName, PackSdf.Input packSdfInput) {
        final String depFilename = "make-permissive." + sdfName + ".dep";
        final MakePermissive.Input makePermissiveInput =
                new MakePermissive.Input(context, makePermissiveOutputPath, depFilename, packSdfInput);

        return new Sdf2Table.Input(context, sdf2tableOutputPath, makePermissiveInput);
    }

    private void sdf2Table(Sdf2Table.Input sdf2TableInput) throws IOException {
        requireBuild(Sdf2Table.factory, sdf2TableInput);
    }

    private void metaSdf2Table(Sdf2Table.Input sdf2TableInput) throws IOException {
        require(sdf2TableInput.inputModule(), SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
                : LastModifiedStamper.instance);
        if (FileCommands.exists(sdf2TableInput.inputModule())) {
            final FileObject strategoMixPath = context.resourceService().resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = toFileReplicate(strategoMixPath);
            sdf2TableInput.sdfArgs().addFile("-Idef", strategoMixFile);
            provide(strategoMixFile);
            requireBuild(Sdf2Table.factory, sdf2TableInput);
        }
    }

    public static PPGen.Input ppGenInput(SpoofaxContext context, File ppGenInputPath, File ppGenOutputPath, File afGenOutputPath, String module, Origin origin) {
        return new PPGen.Input(context, ppGenInputPath, ppGenOutputPath, afGenOutputPath, module, origin);
    }

    private void ppGen(PPGen.Input input) throws IOException {
        requireBuild(PPGen.factory, input);
    }

    public static PPPack.Input ppPackInput(SpoofaxContext context, File ppPackInputPath, File ppPackOutputPath, Origin origin) {
        return new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, origin);
    }

    private void ppPack(PPPack.Input input) throws IOException {
        requireBuild(PPPack.factory, input);
    }

    public static Sdf2Parenthesize.Input sdf2ParenthesizeInput(SpoofaxContext context, Input input, Origin origin) {
        return new Sdf2Parenthesize.Input(context, input.sdf2ParenthesizeInputFile, input.sdf2ParenthesizeOutputFile, input.sdf2ParenthesizeOutputModule, input.sdfName, origin);
    }

    private Origin sdf2Parenthesize(Sdf2Parenthesize.Input input) {
        return Sdf2Parenthesize.origin(input);
    }

    public static Sdf2Rtg.Input sdf2Rtg(SpoofaxContext context, Input input, Origin origin) {
        return new Sdf2Rtg.Input(context, input.sdf2RtgInputFile, input.sdf2RtgOutputFile, input.sdfName, origin);
    }

    public static Rtg2Sig.Input rtg2SigInput(SpoofaxContext context, Input input, final Sdf2Rtg.Input sdf2RtgInput) {
        return new Rtg2Sig.Input(context, input.rtg2SigOutputFile, input.strategoMainFile, sdf2RtgInput);
    }

    private Origin rtg2Sig(Rtg2Sig.Input input) {
        return Rtg2Sig.origin(input);
    }

    private void strj(Input input, Origin origin) throws IOException {
        requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externalJar, input.strjTarget));

        final Iterable<FileObject> paths =
                context.languagePathService().sourceAndIncludePaths(context.languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for (FileObject path : paths) {
            final File file = toFileReplicate(path);
            includeDirs.add(file);
        }

        final Arguments strategoArgs = strategoArgs(input.strategoArgs, input.format, input.strategiesPackageName, input.externalJarFlags, input.strategoJavaStrategiesMainFile);

        requireBuild(Strj.factory, new Strj.Input(context, input.strjInputFile, input.strjOutputFile, input.strjDepFile, "trans", true, true,
                Iterables.toArray(includeDirs, File.class), new String[0], input.strjCacheDir, strategoArgs, origin));
    }

    private Arguments strategoArgs(Arguments strategoArgs, StrategoFormat format, String strategiesPackageName, @Nullable String externalJarFlags, File strategoJavaStrategiesMainFile) {
        final Arguments args = new Arguments();
        args.addAll(strategoArgs);

        if (format == StrategoFormat.ctree) {
            args.add("-F");
        } else {
            args.add("-la", "java-front");
            if (context.isJavaJarEnabled(this, strategoJavaStrategiesMainFile)) {
                args.add("-la", strategiesPackageName);
            }
        }

        if (externalJarFlags != null) {
            args.addLine(externalJarFlags);
        }

        return args;
    }
}
