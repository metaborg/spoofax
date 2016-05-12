package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.MakePermissive;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Parenthesize;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;

public class GenerateSourcesBuilder extends SpoofaxBuilder<GenerateSourcesBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final @Nullable String sdfModule;
        public final @Nullable File sdfFile;
        public final SdfVersion sdfVersion;
        public final @Nullable File sdfExternalDef;
        public final List<File> packSdfIncludePaths;
        public final Arguments packSdfArgs;

        public final @Nullable String sdfMetaModule;
        public final @Nullable File sdfMetaFile;

        public final @Nullable File strFile;
        public final @Nullable String strJavaStratPackage;
        public final @Nullable File strJavaStratFile;
        public final StrategoFormat strFormat;
        public final @Nullable File strExternalJar;
        public final @Nullable String strExternalJarFlags;
        public final List<File> strjIncludeDirs;
        public final Arguments strjArgs;


        public Input(SpoofaxContext context, @Nullable String sdfModule, @Nullable File sdfFile, SdfVersion sdfVersion,
            @Nullable File sdfExternalDef, List<File> packSdfIncludePaths, Arguments packSdfArgs,
            @Nullable String sdfMetaModule, @Nullable File sdfMetaFile, @Nullable File strFile,
            @Nullable String strJavaStratPackage, @Nullable File strJavaStratFile, StrategoFormat strFormat,
            @Nullable File strExternalJar, @Nullable String strExternalJarFlags, List<File> strjIncludeDirs,
            Arguments strjArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfFile = sdfFile;
            this.sdfVersion = sdfVersion;
            this.sdfExternalDef = sdfExternalDef;
            this.packSdfIncludePaths = packSdfIncludePaths;
            this.packSdfArgs = packSdfArgs;
            this.sdfMetaModule = sdfMetaModule;
            this.sdfMetaFile = sdfMetaFile;
            this.strFile = strFile;
            this.strJavaStratPackage = strJavaStratPackage;
            this.strJavaStratFile = strJavaStratFile;
            this.strFormat = strFormat;
            this.strExternalJar = strExternalJar;
            this.strExternalJarFlags = strExternalJarFlags;
            this.strjIncludeDirs = strjIncludeDirs;
            this.strjArgs = strjArgs;
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
        final File srcGenSigDir = toFile(paths.syntaxSrcGenSignatureDir());
        final File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());

        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());

        // SDF
        final @Nullable Origin parenthesizeOrigin;
        final @Nullable Origin sigOrigin;
        if(input.sdfModule != null) {
            final String sdfModule = input.sdfModule;
            final File sdfFile = input.sdfFile;

            // Get the SDF def file, either from existing external def, or by running pack SDF on the grammar
            // specification.
            final @Nullable File packSdfFile;
            final @Nullable Origin packSdfOrigin;
            if(input.sdfExternalDef != null) {
                packSdfFile = input.sdfExternalDef;
                packSdfOrigin = null;
            } else if(sdfFile != null) {
                require(sdfFile, FileExistsStamper.instance);
                if(!sdfFile.exists()) {
                    throw new IOException("Main SDF file at " + sdfFile + " does not exist");
                }

                packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + ".def");
                packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfModule, sdfFile, packSdfFile,
                    input.packSdfIncludePaths, input.packSdfArgs, null));
            } else {
                packSdfFile = null;
                packSdfOrigin = null;
            }

            if(packSdfFile != null) {
                // Get Stratego signatures file when using an external def, or when using sdf2, from the SDF def file.
                if(input.sdfExternalDef != null || input.sdfVersion == SdfVersion.sdf2) {
                    final File rtgFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".rtg");
                    final Origin rtgOrigin =
                        Sdf2Rtg.origin(new Sdf2Rtg.Input(context, packSdfFile, rtgFile, sdfModule, packSdfOrigin));
                    final File sigFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".str");
                    final String sigModule = "signatures/" + sdfModule;
                    sigOrigin = Rtg2Sig.origin(new Rtg2Sig.Input(context, rtgFile, sigFile, sigModule, rtgOrigin));
                } else {
                    sigOrigin = null;
                }

                // Get Stratego parenthesizer file, from the SDF def file.
                final File parenthesizeFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
                final String parenthesizeModule = "pp/" + sdfModule + "-parenthesize";
                parenthesizeOrigin = Sdf2Parenthesize.origin(new Sdf2Parenthesize.Input(context, packSdfFile,
                    parenthesizeFile, sdfModule, parenthesizeModule, packSdfOrigin));

                // Get SDF permissive def file, from the SDF def file.
                final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + "-permissive.def");
                final Origin permissiveDefOrigin = MakePermissive.origin(
                    new MakePermissive.Input(context, packSdfFile, permissiveDefFile, sdfModule, packSdfOrigin));

                // Get JSGLR parse table, from the SDF permissive def file.
                final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf.tbl");
                final Origin sdf2TableOrigin = Sdf2Table
                    .origin(new Sdf2Table.Input(context, permissiveDefFile, tableFile, sdfModule, permissiveDefOrigin));

                requireBuild(sdf2TableOrigin);
            } else {
                parenthesizeOrigin = null;
                sigOrigin = null;
            }
        } else {
            parenthesizeOrigin = null;
            sigOrigin = null;
        }

        // SDF meta-module for creating a Stratego concrete syntax extension parse table
        final File sdfMetaFile = input.sdfMetaFile;
        final Origin sdfMetaOrigin;
        if(sdfMetaFile != null) {
            require(sdfMetaFile, FileExistsStamper.instance);
            if(!sdfMetaFile.exists()) {
                throw new IOException("Main meta-SDF file at " + sdfMetaFile + " does not exist");
            }
            final String sdfMetaModule = input.sdfMetaModule;

            final FileObject strategoMixPath = context.resourceService().resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = toFileReplicate(strategoMixPath);
            final Arguments packSdfMetaArgs = new Arguments(input.packSdfArgs);
            packSdfMetaArgs.addFile("-Idef", strategoMixFile);
            provide(strategoMixFile);

            final File packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + ".def");
            final Origin packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfMetaModule, sdfMetaFile,
                packSdfFile, input.packSdfIncludePaths, packSdfMetaArgs, null));

            final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + "-permissive.def");
            final Origin permissiveDefOrigin = MakePermissive.origin(
                new MakePermissive.Input(context, packSdfFile, permissiveDefFile, sdfMetaModule, packSdfOrigin));

            final File transDir = toFile(paths.transDir());
            final File tableFile = FileUtils.getFile(transDir, sdfMetaModule + ".tbl");
            sdfMetaOrigin = Sdf2Table
                .origin(new Sdf2Table.Input(context, permissiveDefFile, tableFile, sdfMetaModule, permissiveDefOrigin));
            requireBuild(sdfMetaOrigin);
        } else {
            sdfMetaOrigin = null;
        }

        // Stratego
        final File strFile = input.strFile;
        if(strFile != null) {
            require(strFile, FileExistsStamper.instance);
            if(!strFile.exists()) {
                throw new IOException("Main Stratego file at " + strFile + " does not exist");
            }

            boolean buildStrJavaStrat = input.strJavaStratPackage != null && input.strJavaStratFile != null;
            if(buildStrJavaStrat) {
                require(input.strJavaStratFile, FileExistsStamper.instance);
                if(!input.strJavaStratFile.exists()) {
                    throw new IOException(
                        "Main Stratego Java strategies file at " + input.strJavaStratFile + " does not exist");
                }
            }

            final Arguments extraArgs = new Arguments();
            extraArgs.addAll(input.strjArgs);

            final File outputFile;
            final File depPath;
            if(input.strFormat == StrategoFormat.ctree) {
                outputFile = FileUtils.getFile(targetMetaborgDir, "stratego.ctree");
                depPath = outputFile;
                extraArgs.add("-F");
            } else {
                depPath = toFile(paths.strSrcGenJavaTransDir());
                outputFile = FileUtils.getFile(depPath, "Main.java");
                extraArgs.add("-la", "java-front");
                if(buildStrJavaStrat) {
                    extraArgs.add("-la", input.strJavaStratPackage);
                }
            }

            if(input.strExternalJarFlags != null) {
                extraArgs.addLine(input.strExternalJarFlags);
            }

            // @formatter:off
            final Origin origin = Origin.Builder()
                .add(parenthesizeOrigin)
                .add(sigOrigin)
                .add(sdfMetaOrigin)
                .get();
            // @formatter:on

            final File cacheDir = toFile(paths.strCacheDir());

            final Strj.Input strjInput = new Strj.Input(context, strFile, outputFile, depPath, "trans", true, true,
                input.strjIncludeDirs, Lists.<String>newArrayList(), cacheDir, extraArgs, origin);
            final Origin strjOrigin = Strj.origin(strjInput);
            requireBuild(strjOrigin);
        }

        return None.val;
    }
}
