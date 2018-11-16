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
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoBuildSetting;
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
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2ParenthesizeLegacy;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2TableLegacy;
import org.metaborg.spoofax.meta.core.pluto.build.StrIncr;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.meta.core.pluto.build.Typesmart;
import org.metaborg.spoofax.meta.core.pluto.build.misc.GetStrategoMix;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
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
        public final JSGLRVersion jsglrVersion;
        public final @Nullable File sdfExternalDef;
        public final List<File> packSdfIncludePaths;
        public final Arguments packSdfArgs;


        public final @Nullable String sdfCompletionModule;
        public final @Nullable File sdfCompletionFile;

        public final @Nullable List<String> sdfMetaModules;
        public final @Nullable List<File> sdfMetaFiles;

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
        public final StrategoBuildSetting strBuildSetting;


        public Input(SpoofaxContext context, String languageId, Collection<LanguageIdentifier> sourceDeps,
            @Nullable Boolean sdfEnabled, @Nullable String sdfModule, @Nullable File sdfFile, JSGLRVersion jsglrVersion,
            SdfVersion sdfVersion, Sdf2tableVersion sdf2tableVersion, @Nullable File sdfExternalDef,
            List<File> packSdfIncludePaths, Arguments packSdfArgs, @Nullable String sdfCompletionModule,
            @Nullable File sdfCompletionFile, @Nullable List<String> sdfMetaModules, @Nullable List<File> sdfMetaFiles,
            @Nullable File strFile, @Nullable String strJavaPackage, @Nullable String strJavaStratPackage,
            @Nullable File strJavaStratFile, StrategoFormat strFormat, @Nullable File strExternalJar,
            @Nullable String strExternalJarFlags, List<File> strjIncludeDirs, List<File> strjIncludeFiles,
            Arguments strjArgs, StrategoBuildSetting strBuildSetting) {
            super(context);
            this.languageId = languageId;
            this.sdfEnabled = sdfEnabled;
            this.sourceDeps = sourceDeps;
            this.sdfModule = sdfModule;
            this.sdfFile = sdfFile;
            this.jsglrVersion = jsglrVersion;
            this.sdfVersion = sdfVersion;
            this.sdf2tableVersion = sdf2tableVersion;
            this.sdfExternalDef = sdfExternalDef;
            this.packSdfIncludePaths = packSdfIncludePaths;
            this.packSdfArgs = packSdfArgs;
            this.sdfCompletionModule = sdfCompletionModule;
            this.sdfCompletionFile = sdfCompletionFile;
            this.sdfMetaModules = sdfMetaModules;
            this.sdfMetaFiles = sdfMetaFiles;
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
            this.strBuildSetting = strBuildSetting;
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

    private Sdf2Table.Input newParseTableGeneration(GenerateSourcesBuilder.Input input) {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());

        final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic
            || input.sdf2tableVersion == Sdf2tableVersion.incremental);
        final boolean dataDependent = (input.jsglrVersion == JSGLRVersion.dataDependent);
        final boolean layoutSensitive = (input.jsglrVersion == JSGLRVersion.layoutSensitive);

        final File srcNormDir = toFile(paths.syntaxNormDir());
        final File sdfNormFile = FileUtils.getFile(srcNormDir, input.sdfModule + "-norm.aterm");
        
        final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf.tbl");
        final File persistedTableFile = FileUtils.getFile(targetMetaborgDir, "table.bin");
        final File contextualGrammarFile = FileUtils.getFile(targetMetaborgDir, "ctxgrammar.aterm");

        final List<String> paths = srcGenNormalizedSdf3Paths(input);

        return new Sdf2Table.Input(context, sdfNormFile, tableFile, persistedTableFile, contextualGrammarFile, paths,
            dynamicGeneration, dataDependent, layoutSensitive);
    }

    private Sdf2Table.Input newParseTableGenerationCompletions(GenerateSourcesBuilder.Input input) {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());

        final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic
            || input.sdf2tableVersion == Sdf2tableVersion.incremental);
        final boolean dataDependent = (input.jsglrVersion == JSGLRVersion.dataDependent);
        final boolean layoutSensitive = (input.jsglrVersion == JSGLRVersion.layoutSensitive);
        
        final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf-completions.tbl");
        final File persistedTableFile = FileUtils.getFile(targetMetaborgDir, "table-completions.bin");
        final List<String> paths = srcGenNormalizedSdf3Paths(input);

        return new Sdf2Table.Input(context, input.sdfCompletionFile, tableFile,
            persistedTableFile, null, paths, dynamicGeneration, dataDependent, layoutSensitive);
    }
    
    private PackSdfBuild oldParseTableGenerationPack(GenerateSourcesBuilder.Input input, File srcGenSyntaxDir, String sdfModule, File sdfFile, File sdfExternalDef) throws IOException {
        // Get the SDF def file, either from existing external def, or by running pack SDF on the grammar
        // specification.
        final @Nullable File packSdfFile;
        final @Nullable Origin packSdfOrigin;
        
        if(sdfExternalDef != null) {
            packSdfFile = sdfExternalDef;
            packSdfOrigin = null;
        } else if(sdfFile != null) {
            require(sdfFile, FileExistsStamper.instance);
            if(!sdfFile.exists()) {
                // TODO indicate completions in error
                throw new IOException("Main SDF file at " + sdfFile + " does not exist");
            }

            packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + ".def");
            packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfModule, sdfFile, packSdfFile,
                input.packSdfIncludePaths, input.packSdfArgs, null));
        } else {
            packSdfFile = null;
            packSdfOrigin = null;
        }
        
        return new PackSdfBuild(packSdfFile, packSdfOrigin);
    }
    
    private class PackSdfBuild {
        
        final @Nullable File file;
        final @Nullable Origin origin;
        
        PackSdfBuild(File packSdfFile, Origin packSdfOrigin) {
            this.file = packSdfFile;
            this.origin = packSdfOrigin;
        }

    }
    
    private Origin oldParseTableGeneration(GenerateSourcesBuilder.Input input, build.pluto.dependency.Origin.Builder sdfBuilder, PackSdfBuild packSdfBuild, File srcGenSyntaxDir, String sdfModule, File sdfExternalDef, String parseTableFilename, String modulePrefix, boolean isCompletions) throws IOException {
        if(packSdfBuild.file != null) {
            if (!isCompletions) {
                // Get Stratego signatures file when using an external def, or when using sdf2, from the SDF def
                // file.
                if(sdfExternalDef != null || input.sdfVersion == SdfVersion.sdf2) {
                    final File srcGenSigDir = toFile(paths.syntaxSrcGenSignatureDir());
                    final File rtgFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".rtg");
                    final Origin rtgOrigin =
                        Sdf2Rtg.origin(new Sdf2Rtg.Input(context, packSdfBuild.file, rtgFile, sdfModule, packSdfBuild.origin));
                    final File sigFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".str");
                    final String sigModule = "signatures/" + sdfModule;
                    final Origin sigOrigin = Rtg2Sig.origin(new Rtg2Sig.Input(context, rtgFile, sigFile, sigModule, rtgOrigin));
                    
                    sdfBuilder.add(sigOrigin);
                }
    
                // Get Stratego parenthesizer file, from the SDF def file.
                final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());
                final File parenthesizeFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
                final String parenthesizeModule = "pp/" + sdfModule + "-parenthesize";
                final Origin parenthesizeOrigin = Sdf2ParenthesizeLegacy.origin(new Sdf2ParenthesizeLegacy.Input(context,
                    packSdfBuild.file, parenthesizeFile, sdfModule, parenthesizeModule, packSdfBuild.origin));
    
                sdfBuilder.add(parenthesizeOrigin);
            }
            
            // Get SDF permissive def file, from the SDF def file.
            final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + "-permissive.def");
            final Origin permissiveDefOrigin = MakePermissive.origin(
                new MakePermissive.Input(context, packSdfBuild.file, permissiveDefFile, sdfModule, packSdfBuild.origin));

            // Get JSGLR parse table, from the SDF permissive def file.
            final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
            final File tableFile = FileUtils.getFile(targetMetaborgDir, parseTableFilename);
            return Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context,
                permissiveDefFile, tableFile, modulePrefix + sdfModule, permissiveDefOrigin));
        } else {
            return null;
        }
    }
    
    private void newParseTableGenerationBuild(GenerateSourcesBuilder.Input input, build.pluto.dependency.Origin.Builder sdfBuilder) throws IOException {
        final Sdf2Table.Input sdf2TableJavaInput = newParseTableGeneration(input);
        final Origin sdf2TableJavaOrigin = Sdf2Table.origin(sdf2TableJavaInput);

        requireBuild(sdf2TableJavaOrigin);

        // New parenthesizer
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());
        
        final File parenthesizerFile = FileUtils.getFile(srcGenPpDir, input.sdfModule + "-parenthesize.str");
        final Origin javaParenthesizeOrigin = Sdf2Parenthesize.origin(
            new Sdf2Parenthesize.Input(context, sdf2TableJavaInput.outputFile, parenthesizerFile, input.sdfModule));
        
        sdfBuilder.add(javaParenthesizeOrigin);

        // Completions
        if(input.sdfCompletionFile != null && input.sdfEnabled) {
            Sdf2Table.Input sdf2TableJavaInputCompletions = newParseTableGenerationCompletions(input);
            final Origin sdfCompletionOrigin = Sdf2Table.origin(sdf2TableJavaInputCompletions);

            requireBuild(sdfCompletionOrigin);
            
            sdfBuilder.add(sdfCompletionOrigin);
        }
    }

    private void oldParseTableGenerationBuild(GenerateSourcesBuilder.Input input, build.pluto.dependency.Origin.Builder sdfBuilder) throws IOException {
        File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        PackSdfBuild packSdfBuild = oldParseTableGenerationPack(input, srcGenSyntaxDir, input.sdfModule, input.sdfFile, input.sdfExternalDef);
        final Origin sdfOrigin = oldParseTableGeneration(input, sdfBuilder, packSdfBuild, srcGenSyntaxDir, input.sdfModule, input.sdfExternalDef, "sdf.tbl", "", false);
        
        requireBuild(sdfOrigin);
        
        // Completions
        if(input.sdfCompletionFile != null && input.sdfEnabled) {
            File srcGenSyntaxCompletionsDir = toFile(paths.syntaxCompletionSrcGenDir());
            PackSdfBuild packSdfCompletionsBuild = oldParseTableGenerationPack(input, srcGenSyntaxCompletionsDir, input.sdfCompletionModule, input.sdfCompletionFile, null);
            final Origin sdfCompletionOrigin = oldParseTableGeneration(input, sdfBuilder, packSdfCompletionsBuild, srcGenSyntaxCompletionsDir, input.sdfCompletionModule, null, "sdf-completions.tbl", "completion/", true);
            
            requireBuild(sdfCompletionOrigin);
            
            sdfBuilder.add(sdfCompletionOrigin);
        }
    }
    
    private build.pluto.dependency.Origin.Builder buildSdf(GenerateSourcesBuilder.Input input) throws IOException {
        build.pluto.dependency.Origin.Builder sdfBuilder = Origin.Builder();
        
        if(input.sdfModule != null && input.sdfEnabled) {
            // new parse table generator
            if(input.sdf2tableVersion == Sdf2tableVersion.java || input.sdf2tableVersion == Sdf2tableVersion.dynamic
                || input.sdf2tableVersion == Sdf2tableVersion.incremental) {
                newParseTableGenerationBuild(input, sdfBuilder);
            } else {
                oldParseTableGenerationBuild(input, sdfBuilder);
            }
        }
        
        return sdfBuilder;
    }
    
    private List<Origin> buildSdfMeta(GenerateSourcesBuilder.Input input) throws IOException {
        final File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        
        final List<Origin> sdfMetaOrigins = Lists.newArrayList();

        for(int i = 0; i < input.sdfMetaFiles.size(); i++) {
            final File sdfMetaFile = input.sdfMetaFiles.get(i);

            if(sdfMetaFile != null) {
                require(sdfMetaFile, FileExistsStamper.instance);
                if(!sdfMetaFile.exists()) {
                    throw new IOException("Main meta-SDF file at " + sdfMetaFile + " does not exist");
                }
                final String sdfMetaModule = input.sdfMetaModules.get(i);

                final BuildRequest<GetStrategoMix.Input, OutputPersisted<File>, GetStrategoMix, SpoofaxBuilderFactory<GetStrategoMix.Input, OutputPersisted<File>, GetStrategoMix>> getStrategoMixRequest =
                    GetStrategoMix.request(new GetStrategoMix.Input(context));
                final File strategoMixFile = requireBuild(getStrategoMixRequest).val();
                final Origin strategoMixOrigin = Origin.from(getStrategoMixRequest);
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
                sdfMetaOrigins.add(Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context, permissiveDefFile,
                    tableFile, sdfMetaModule, permissiveDefOrigin)));
                requireBuild(sdfMetaOrigins.get(i));
            }
        }
        
        return sdfMetaOrigins;
    }
    
    private void buildStratego(GenerateSourcesBuilder.Input input, Origin sdfOrigin) throws IOException {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
        
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

            final File cacheDir = toFile(paths.strCacheDir());

            if(input.strBuildSetting == StrategoBuildSetting.incremental) {
                final StrIncr.Input strIncrInput = new StrIncr.Input(context, strFile, input.strJavaPackage,
                    input.strjIncludeDirs, input.strjIncludeFiles, cacheDir, extraArgs, depPath, sdfOrigin);
                requireBuild(StrIncr.request(strIncrInput));
            } else {
                final Strj.Input strjInput =

                    new Strj.Input(context, strFile, outputFile, depPath, input.strJavaPackage, true, true,
                        input.strjIncludeDirs, input.strjIncludeFiles, Lists.newArrayList(), cacheDir, extraArgs,
                        sdfOrigin);

                final Origin strjOrigin = Strj.origin(strjInput);
                requireBuild(strjOrigin);
            }

            // Typesmart
            final File typesmartExportedFile = toFile(paths.strTypesmartExportedFile());
            final Typesmart.Input typesmartInput =
                new Typesmart.Input(context, input.strFile, input.strjIncludeDirs, typesmartExportedFile, sdfOrigin);
            final Origin typesmartOrigin = Typesmart.origin(typesmartInput);
            requireBuild(typesmartOrigin);
        }
    }
    
    @Override public None build(GenerateSourcesBuilder.Input input) throws IOException {
        // SDF
        build.pluto.dependency.Origin.Builder sdfBuilder = buildSdf(input);

        // SDF meta-module for creating a Stratego concrete syntax extension parse table
        List<Origin> sdfMetaOrigins = buildSdfMeta(input);
        
        for(Origin sdfMetaOrigin :  sdfMetaOrigins) {
            sdfBuilder = sdfBuilder.add(sdfMetaOrigin);
        }
        
        final Origin sdfOrigin = sdfBuilder.get();

        // Stratego
        buildStratego(input, sdfOrigin);

        return None.val;
    }
    
    private List<String> srcGenNormalizedSdf3Paths(GenerateSourcesBuilder.Input input) {
        File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        
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
                            if(export.language.equals(SpoofaxConstants.LANG_ATERM_NAME)) {
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
        
        return paths;
    }
}
