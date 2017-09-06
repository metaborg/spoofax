package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.meta.core.config.Sdf2tableVersion;
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
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2TableNew;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.meta.core.pluto.build.Typesmart;
import org.metaborg.spoofax.meta.core.pluto.build.misc.PrepareNativeBundle;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.FileExistsStamper;

public class GenerateSourcesBuilder extends SpoofaxBuilder<GenerateSourcesBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final String languageId;
        public final @Nullable Collection<LanguageIdentifier> sourceDeps;

        public final @Nullable String sdfModule;
        public final @Nullable Boolean sdfEnabled;
        public final @Nullable File sdfFile;
        public final SdfVersion sdfVersion;
        public final Sdf2tableVersion sdf2tableVersion;
        public final @Nullable File sdfExternalDef;
        public final List<File> packSdfIncludePaths;
        public final Arguments packSdfArgs;


        public final @Nullable String sdfCompletionModule;
        public final @Nullable File sdfCompletionFile;

        public final @Nullable String sdfMetaModule;
        public final @Nullable File sdfMetaFile;

        public final @Nullable File strFile;
        public final @Nullable String strJavaPackage;
        public final @Nullable String strJavaStratPackage;
        public final @Nullable File strJavaStratFile;
        public final StrategoFormat strFormat;
        public final @Nullable File strExternalJar;
        public final @Nullable String strExternalJarFlags;
        public final List<File> strjIncludeDirs;
        public final List<File> strjIncludeFiles;
        public final Arguments strjArgs;


        public Input(SpoofaxContext context, String languageId, Collection<LanguageIdentifier> sourceDeps,
            @Nullable Boolean sdfEnabled, @Nullable String sdfModule, @Nullable File sdfFile, SdfVersion sdfVersion,
            Sdf2tableVersion sdf2tableVersion, @Nullable File sdfExternalDef, List<File> packSdfIncludePaths,
            Arguments packSdfArgs, @Nullable String sdfCompletionModule, @Nullable File sdfCompletionFile,
            @Nullable String sdfMetaModule, @Nullable File sdfMetaFile, @Nullable File strFile,
            @Nullable String strJavaPackage, @Nullable String strJavaStratPackage, @Nullable File strJavaStratFile,
            StrategoFormat strFormat, @Nullable File strExternalJar, @Nullable String strExternalJarFlags,
            List<File> strjIncludeDirs, List<File> strjIncludeFiles, Arguments strjArgs) {
            super(context);
            this.languageId = languageId;
            this.sdfEnabled = sdfEnabled;
            this.sourceDeps = sourceDeps;
            this.sdfModule = sdfModule;
            this.sdfFile = sdfFile;
            this.sdfVersion = sdfVersion;
            this.sdf2tableVersion = sdf2tableVersion;
            this.sdfExternalDef = sdfExternalDef;
            this.packSdfIncludePaths = packSdfIncludePaths;
            this.packSdfArgs = packSdfArgs;
            this.sdfCompletionModule = sdfCompletionModule;
            this.sdfCompletionFile = sdfCompletionFile;
            this.sdfMetaModule = sdfMetaModule;
            this.sdfMetaFile = sdfMetaFile;
            this.strFile = strFile;
            this.strJavaPackage = strJavaPackage;
            this.strJavaStratPackage = strJavaStratPackage;
            this.strJavaStratFile = strJavaStratFile;
            this.strFormat = strFormat;
            this.strExternalJar = strExternalJar;
            this.strExternalJarFlags = strExternalJarFlags;
            this.strjIncludeDirs = strjIncludeDirs;
            this.strjIncludeFiles = strjIncludeFiles;
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
        final File srcGenSyntaxCompletionDir = toFile(paths.syntaxCompletionSrcGenDir());
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());

        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());

        // SDF
        final @Nullable Origin parenthesizeOrigin;
        final @Nullable Origin sigOrigin;
        if(input.sdfModule != null && input.sdfEnabled) {
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

                // Get SDF permissive def file, from the SDF def file.
                final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + "-permissive.def");
                final Origin permissiveDefOrigin = MakePermissive.origin(
                    new MakePermissive.Input(context, packSdfFile, permissiveDefFile, sdfModule, packSdfOrigin));

                if(input.sdf2tableVersion == Sdf2tableVersion.java
                    || input.sdf2tableVersion == Sdf2tableVersion.dynamic) {
                    // Get JSGLR parse table from the normalized SDF aterm
                    final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic);
                    final File srcNormDir = toFile(paths.syntaxNormDir());
                    final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf-new.tbl");
                    final File contextualGrammarFile = FileUtils.getFile(targetMetaborgDir, "ctxgrammar.aterm");
                    final File normGrammarFile = FileUtils.getFile(targetMetaborgDir, "normgrammar.bin");
                    File sdfNormFile = FileUtils.getFile(srcNormDir, sdfModule + "-norm.aterm");
                    final List<String> paths = Lists.newLinkedList();
                    paths.add(srcGenSyntaxDir.getAbsolutePath());

                    for(LanguageIdentifier langId : input.sourceDeps) {
                        ILanguageImpl lang = context.languageService().getImpl(langId);
                        for(final ILanguageComponent component : lang.components()) {
                            ILanguageComponentConfig config = component.config();
                            Collection<IExportConfig> exports = config.exports();
                            for(IExportConfig exportConfig : exports) {
                                exportConfig.accept(new IExportVisitor() {
                                    @Override public void visit(LangDirExport export) {
                                        if(export.language.equals("ATerm")) {
                                            try {
                                                paths.add(
                                                    toFileReplicate(component.location().resolveFile(export.directory))
                                                        .getAbsolutePath());
                                            } catch(FileSystemException e) {
                                                System.out.println("Failed to locate path");
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override public void visit(LangFileExport export) {
                                        // Ignore file exports
                                    }

                                    @Override public void visit(ResourceExport export) {
                                        // Ignore resource exports

                                    }
                                });
                            }
                        }
                    }

                    final Origin sdf2TableJavaOrigin = Sdf2TableNew.origin(new Sdf2TableNew.Input(context, sdfNormFile,
                        tableFile, normGrammarFile, contextualGrammarFile, paths, true, dynamicGeneration));

                    requireBuild(sdf2TableJavaOrigin);
                }

                // Get Stratego parenthesizer file, from the SDF def file.
                final File parenthesizeFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
                final String parenthesizeModule = "pp/" + sdfModule + "-parenthesize";
                parenthesizeOrigin = Sdf2Parenthesize.origin(new Sdf2Parenthesize.Input(context, packSdfFile,
                    parenthesizeFile, sdfModule, parenthesizeModule, packSdfOrigin));


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

        // SDF completions
        final Origin sdfCompletionOrigin;
        if(input.sdfCompletionFile != null && input.sdfEnabled) {
            final String sdfCompletionsModule = input.sdfCompletionModule;
            final File sdfCompletionsFile = input.sdfCompletionFile;

            if(input.sdf2tableVersion == Sdf2tableVersion.java || input.sdf2tableVersion == Sdf2tableVersion.dynamic) {
                // Get JSGLR parse table, from the normalized SDF aterm

                final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic);
                final List<String> paths = Lists.newLinkedList();
                paths.add(srcGenSyntaxDir.getAbsolutePath());

                for(LanguageIdentifier langId : input.sourceDeps) {
                    ILanguageImpl lang = context.languageService().getImpl(langId);
                    for(final ILanguageComponent component : lang.components()) {
                        ILanguageComponentConfig config = component.config();
                        Collection<IExportConfig> exports = config.exports();
                        for(IExportConfig exportConfig : exports) {
                            exportConfig.accept(new IExportVisitor() {
                                @Override public void visit(LangDirExport export) {
                                    if(export.language.equals("ATerm")) {
                                        try {
                                            paths
                                                .add(toFileReplicate(component.location().resolveFile(export.directory))
                                                    .getAbsolutePath());
                                        } catch(FileSystemException e) {
                                            System.out.println("Failed to locate path");
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override public void visit(LangFileExport export) {
                                    // Ignore file exports
                                }

                                @Override public void visit(ResourceExport export) {
                                    // Ignore resource exports

                                }
                            });
                        }
                    }
                }

                final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf-completions.tbl");
                sdfCompletionOrigin = Sdf2TableNew.origin(new Sdf2TableNew.Input(context, sdfCompletionsFile, tableFile,
                    null, null, paths, false, dynamicGeneration));

                requireBuild(sdfCompletionOrigin);
            } else {

                // Get the SDF def file, either from existing external def, or by running pack SDF on the grammar
                // specification.
                final @Nullable File packSdfCompletionsFile;
                final @Nullable Origin packSdfCompletionsOrigin;
                if(sdfCompletionsFile != null) {
                    require(sdfCompletionsFile, FileExistsStamper.instance);
                    if(!sdfCompletionsFile.exists()) {
                        throw new IOException("Main SDF completions file at " + sdfCompletionsFile + " does not exist");
                    }

                    packSdfCompletionsFile =
                        FileUtils.getFile(srcGenSyntaxCompletionDir, sdfCompletionsModule + ".def");
                    packSdfCompletionsOrigin =
                        PackSdf.origin(new PackSdf.Input(context, sdfCompletionsModule, sdfCompletionsFile,
                            packSdfCompletionsFile, input.packSdfIncludePaths, input.packSdfArgs, null));
                } else {
                    packSdfCompletionsFile = null;
                    packSdfCompletionsOrigin = null;
                }

                if(packSdfCompletionsFile != null) {
                    // Get SDF permissive def file, from the SDF def file.
                    final File permissiveCompletionsDefFile =
                        FileUtils.getFile(srcGenSyntaxCompletionDir, sdfCompletionsModule + "-permissive.def");
                    final Origin permissiveCompletionsDefOrigin =
                        MakePermissive.origin(new MakePermissive.Input(context, packSdfCompletionsFile,
                            permissiveCompletionsDefFile, sdfCompletionsModule, packSdfCompletionsOrigin));

                    // Get JSGLR parse table, from the SDF permissive def file.
                    final File completionsTableFile = FileUtils.getFile(targetMetaborgDir, "sdf-completions.tbl");
                    sdfCompletionOrigin = Sdf2Table.origin(new Sdf2Table.Input(context, permissiveCompletionsDefFile,
                        completionsTableFile, "completion/" + sdfCompletionsModule, permissiveCompletionsDefOrigin));

                    requireBuild(sdfCompletionOrigin);
                } else {
                    sdfCompletionOrigin = null;
                }
            }
        } else {
            sdfCompletionOrigin = null;
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

            final BuildRequest<PrepareNativeBundle.Input, OutputTransient<PrepareNativeBundle.Output>, PrepareNativeBundle, SpoofaxBuilderFactory<PrepareNativeBundle.Input, OutputTransient<PrepareNativeBundle.Output>, PrepareNativeBundle>> nativeBundleRequest =
                PrepareNativeBundle.request(new PrepareNativeBundle.Input(context));
            final File strategoMixFile = requireBuild(nativeBundleRequest).val().strategoMixFile;
            final Origin strategoMixOrigin = Origin.from(nativeBundleRequest);
            final Arguments packSdfMetaArgs = new Arguments(input.packSdfArgs);
            packSdfMetaArgs.addFile("-Idef", strategoMixFile);

            final File packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + ".def");

            final Origin packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfMetaModule, sdfMetaFile,
                packSdfFile, input.packSdfIncludePaths, packSdfMetaArgs, strategoMixOrigin));

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
                depPath = toFile(paths.strSrcGenJavaTransDir(input.languageId));
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
            final Origin origin;
            
            if(input.sdf2tableVersion == Sdf2tableVersion.java) {
                origin = Origin.Builder()
                    .add(parenthesizeOrigin)
                    .add(sigOrigin)
                    .add(sdfCompletionOrigin)
                    .add(sdfMetaOrigin)
                    .get();
            } else {
                origin = Origin.Builder()
                    .add(parenthesizeOrigin)
                    .add(sigOrigin)
                    .add(sdfCompletionOrigin)
                    .add(sdfMetaOrigin)
                    .get();
            }
            // @formatter:on

            final File cacheDir = toFile(paths.strCacheDir());

            final Strj.Input strjInput =

                new Strj.Input(context, strFile, outputFile, depPath, input.strJavaPackage, true, true,
                    input.strjIncludeDirs, input.strjIncludeFiles, Lists.newArrayList(), cacheDir, extraArgs, origin);

            final Origin strjOrigin = Strj.origin(strjInput);
            requireBuild(strjOrigin);

            // Typesmart
            final File typesmartExportedFile = toFile(paths.strTypesmartExportedFile());
            final Typesmart.Input typesmartInput =
                new Typesmart.Input(context, input.strFile, input.strjIncludeDirs, typesmartExportedFile, origin);
            final Origin typesmartOrigin = Typesmart.origin(typesmartInput);
            requireBuild(typesmartOrigin);
        }

        return None.val;
    }
}
