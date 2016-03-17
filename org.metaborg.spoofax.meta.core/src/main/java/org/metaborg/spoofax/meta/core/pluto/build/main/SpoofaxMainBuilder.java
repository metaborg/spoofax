package org.metaborg.spoofax.meta.core.pluto.build.main;

import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF_NAME;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
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
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder.Input;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import build.pluto.dependency.Origin;
import build.pluto.output.Output;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

public abstract class SpoofaxMainBuilder<In extends SpoofaxInput, Out extends Output> extends SpoofaxBuilder<In, Out> {
    public SpoofaxMainBuilder(In input) {
        super(input);
    }


    protected static Arguments sdfArgs(SpoofaxContext context, Arguments sdfArgs) {
        final Arguments args = new Arguments();
        args.addAll(sdfArgs);

        final Iterable<FileObject> paths =
            context.languagePathService().sourceAndIncludePaths(context.languageSpec, LANG_SDF_NAME);
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

    protected static PackSdf.Input packSdfInput(SpoofaxContext context, String module, Arguments args, File externalDef,
        File packSdfInputPath, File packSdfOutputPath, File syntaxFolder, File genSyntaxFolder) {
        return new PackSdf.Input(context, module, args, externalDef, packSdfInputPath, packSdfOutputPath, syntaxFolder,
            genSyntaxFolder);
    }

    protected static Sdf2Table.Input sdf2TableInput(SpoofaxContext context, File sdf2tableOutputPath,
        File makePermissiveOutputPath, String sdfName, PackSdf.Input packSdfInput) {
        final String depFilename = "make-permissive." + sdfName + ".dep";
        final MakePermissive.Input makePermissiveInput =
            new MakePermissive.Input(context, makePermissiveOutputPath, depFilename, packSdfInput);

        return new Sdf2Table.Input(context, sdf2tableOutputPath, makePermissiveInput);
    }

    protected void sdf2Table(Sdf2Table.Input sdf2TableInput) throws IOException {
        requireBuild(Sdf2Table.factory, sdf2TableInput);
    }

    protected void metaSdf2Table(Sdf2Table.Input sdf2TableInput) throws IOException {
        require(sdf2TableInput.inputModule(),
            SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
        if(FileCommands.exists(sdf2TableInput.inputModule())) {
            final FileObject strategoMixPath = context.resourceService().resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = toFileReplicate(strategoMixPath);
            sdf2TableInput.sdfArgs().addFile("-Idef", strategoMixFile);
            provide(strategoMixFile);
            requireBuild(Sdf2Table.factory, sdf2TableInput);
        }
    }

    protected static PPGen.Input ppGenInput(SpoofaxContext context, File ppGenInputPath, File ppGenOutputPath,
        File afGenOutputPath, String module, Origin origin) {
        return new PPGen.Input(context, ppGenInputPath, ppGenOutputPath, afGenOutputPath, module, origin);
    }

    protected void ppGen(PPGen.Input input) throws IOException {
        requireBuild(PPGen.factory, input);
    }

    protected static PPPack.Input ppPackInput(SpoofaxContext context, File ppPackInputPath, File ppPackOutputPath,
        Origin origin) {
        return new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, origin);
    }

    protected void ppPack(PPPack.Input input) throws IOException {
        requireBuild(PPPack.factory, input);
    }

    protected static Sdf2Parenthesize.Input sdf2ParenthesizeInput(SpoofaxContext context, Input input, Origin origin) {
        return new Sdf2Parenthesize.Input(context, input.sdf2ParenthesizeInputFile, input.sdf2ParenthesizeOutputFile,
            input.sdf2ParenthesizeOutputModule, input.sdfName, origin);
    }

    protected Origin sdf2Parenthesize(Sdf2Parenthesize.Input input) {
        return Sdf2Parenthesize.origin(input);
    }

    protected static Sdf2Rtg.Input sdf2Rtg(SpoofaxContext context, Input input, Origin origin) {
        return new Sdf2Rtg.Input(context, input.sdf2RtgInputFile, input.sdf2RtgOutputFile, input.sdfName, origin);
    }

    protected static Rtg2Sig.Input rtg2SigInput(SpoofaxContext context, Input input, final Sdf2Rtg.Input sdf2RtgInput) {
        return new Rtg2Sig.Input(context, input.rtg2SigOutputFile, input.strategoMainFile, sdf2RtgInput);
    }

    protected Origin rtg2Sig(Rtg2Sig.Input input) {
        return Rtg2Sig.origin(input);
    }

    protected Origin strj(Input input, Origin sdf2ParenthesizeOrigin, Origin rtg2SigOrigin) throws IOException {
        requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externalJar, input.strjTarget));

        final Iterable<FileObject> paths = context.languagePathService().sourceAndIncludePaths(context.languageSpec,
            SpoofaxConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for(FileObject path : paths) {
            final File file = toFileReplicate(path);
            includeDirs.add(file);
        }

        final Arguments strategoArgs = strategoArgs(input.strategoArgs, input.format, input.strategiesPackageName,
            input.externalJarFlags, input.strategoJavaStrategiesMainFile);

        // @formatter:off
        final Origin origin = Origin.Builder()
            .add(sdf2ParenthesizeOrigin)
            .add(rtg2SigOrigin)
            .get();
        // @formatter:on

        final Strj.Input strjInput = new Strj.Input(context, input.strjInputFile, input.strjOutputFile, input.strjDepFile, "trans", true, true,
            Iterables.toArray(includeDirs, File.class), new String[0], input.strjCacheDir, strategoArgs, origin);
        final Origin strjOrigin = Strj.origin(strjInput);
        requireBuild(strjOrigin);
        return strjOrigin;
    }

    protected Arguments strategoArgs(Arguments strategoArgs, StrategoFormat format, String strategiesPackageName,
        @Nullable String externalJarFlags, File strategoJavaStrategiesMainFile) {
        final Arguments args = new Arguments();
        args.addAll(strategoArgs);

        if(format == StrategoFormat.ctree) {
            args.add("-F");
        } else {
            args.add("-la", "java-front");
            if(context.isJavaJarEnabled(this, strategoJavaStrategiesMainFile)) {
                args.add("-la", strategiesPackageName);
            }
        }

        if(externalJarFlags != null) {
            args.addLine(externalJarFlags);
        }

        return args;
    }
}
