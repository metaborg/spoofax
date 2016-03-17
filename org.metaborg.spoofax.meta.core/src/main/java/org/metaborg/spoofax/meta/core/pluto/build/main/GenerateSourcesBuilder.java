package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.util.cmd.Arguments;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class GenerateSourcesBuilder extends SpoofaxMainBuilder<GenerateSourcesBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File strategoMainFile;
        public final File strategoJavaStrategiesMainFile;
        public final String sdfName;
        public final String metaSdfName;
        public final Arguments sdfArgs;
        @Nullable public final File externalJar;
        @Nullable public final File strjTarget;
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
        @Nullable public final File externalDef;
        public final File packSdfInputPath;
        public final File packSdfOutputPath;
        public final File packMetaSdfInputPath;
        public final File packMetaSdfOutputPath;
        public final File syntaxFolder;
        public final File genSyntaxFolder;
        public final File makePermissiveOutputPath;
        public final File sdf2tableOutputPath;

        public Input(SpoofaxContext context, File strategoMainFile, File strategoJavaStrategiesMainFile, String sdfName,
            String metaSdfName, Arguments sdfArgs, @Nullable File externalJar, @Nullable File strjTarget,
            File strjInputFile, File strjOutputFile, File strjDepFile, File strjCacheDir, Arguments strategoArgs,
            StrategoFormat format, String strategiesPackageName, String externalJarFlags, File rtg2SigOutputFile,
            File sdf2RtgInputFile, File sdf2RtgOutputFile, File sdf2ParenthesizeInputFile,
            File sdf2ParenthesizeOutputFile, String sdf2ParenthesizeOutputModule, File ppPackInputPath,
            File ppPackOutputPath, File ppGenInputPath, File ppGenOutputPath, File afGenOutputPath,
            File makePermissiveOutputPath, File sdf2tableOutputPath, @Nullable File externalDef, File packSdfInputPath,
            File packSdfOutputPath, File packMetaSdfInputPath, File packMetaSdfOutputPath, File syntaxFolder,
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

    public static SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(GenerateSourcesBuilder.class, Input.class);


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

    @Override public None build(GenerateSourcesBuilder.Input input) throws IOException {
        // SDF
        final Arguments sdfArgs = sdfArgs(context, input.sdfArgs);

        final PackSdf.Input packSdfInput = packSdfInput(context, input.sdfName, sdfArgs, input.externalDef,
            input.packSdfInputPath, input.packSdfOutputPath, input.syntaxFolder, input.genSyntaxFolder);
        final Origin packSdfOrigin = PackSdf.origin(packSdfInput);
        final PackSdf.Input metaPackSdfInput =
            packSdfInput(context, input.metaSdfName, new Arguments(sdfArgs), input.externalDef,
                input.packMetaSdfInputPath, input.packMetaSdfOutputPath, input.syntaxFolder, input.genSyntaxFolder);

        sdf2Table(sdf2TableInput(context, input.sdf2tableOutputPath, input.makePermissiveOutputPath, input.sdfName,
            packSdfInput));
        metaSdf2Table(sdf2TableInput(context, input.sdf2tableOutputPath, input.makePermissiveOutputPath,
            input.metaSdfName, metaPackSdfInput));
        ppGen(ppGenInput(context, input.ppGenInputPath, input.ppGenOutputPath, input.afGenOutputPath, input.sdfName,
            packSdfOrigin));
        ppPack(ppPackInput(context, input.ppPackInputPath, input.ppPackOutputPath, packSdfOrigin));
        final Origin sdf2Parenthesize = sdf2Parenthesize(sdf2ParenthesizeInput(context, input, packSdfOrigin));
        final Sdf2Rtg.Input sdf2RtgInput = sdf2Rtg(context, input, packSdfOrigin);
        final Origin rtg2Sig = rtg2Sig(rtg2SigInput(context, input, sdf2RtgInput));

        // Stratego
        if(!context.isBuildStrategoEnabled(this, input.strategoMainFile)) {
            return None.val;
        }

        strj(input, sdf2Parenthesize, rtg2Sig);

        return None.val;
    }
}
