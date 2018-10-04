package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
import org.metaborg.spoofax.meta.core.pluto.build.StrategoIncrementalBackEnd;
import org.metaborg.spoofax.meta.core.pluto.build.StrategoIncrementalFrontEnd;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.meta.core.pluto.build.Typesmart;
import org.metaborg.spoofax.meta.core.pluto.build.misc.GetStrategoMix;
import org.metaborg.util.cmd.Arguments;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileExistsStamper;

public class GenerateSourcesBuilder extends SpoofaxBuilder<GenerateSourcesBuilder.Input, None> {
    private static final int sigOriginOffset = 1;
    private static final int parenthesizeOriginOffset = 0;

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
                @Nullable Boolean sdfEnabled, @Nullable String sdfModule, @Nullable File sdfFile,
                JSGLRVersion jsglrVersion, SdfVersion sdfVersion, Sdf2tableVersion sdf2tableVersion,
                @Nullable File sdfExternalDef, List<File> packSdfIncludePaths, Arguments packSdfArgs,
                @Nullable String sdfCompletionModule, @Nullable File sdfCompletionFile,
                @Nullable List<String> sdfMetaModules, @Nullable List<File> sdfMetaFiles, @Nullable File strFile,
                @Nullable String strJavaPackage, @Nullable String strJavaStratPackage, @Nullable File strJavaStratFile,
                StrategoFormat strFormat, @Nullable File strExternalJar, @Nullable String strExternalJarFlags,
                List<File> strjIncludeDirs, List<File> strjIncludeFiles, Arguments strjArgs, 
                StrategoBuildSetting strBuildSetting) {
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

    public static SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder> factory = SpoofaxBuilderFactoryFactory
            .of(GenerateSourcesBuilder.class, Input.class);

    public GenerateSourcesBuilder(Input input) {
        super(input);
    }

    public static BuildRequest<Input, None, GenerateSourcesBuilder, SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder>> request(
            Input input) {
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
    public None build(GenerateSourcesBuilder.Input input) throws IOException {
        final File srcGenSigDir = toFile(paths.syntaxSrcGenSignatureDir());
        final File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());
        final File srcGenSyntaxCompletionDir = toFile(paths.syntaxCompletionSrcGenDir());
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());

        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());

        // SDF
        final Origin[] sdfOrigins = sdfTasks(input, srcGenSigDir, srcGenSyntaxDir, srcGenPpDir, targetMetaborgDir);
        final @Nullable Origin parenthesizeOrigin = sdfOrigins[parenthesizeOriginOffset];
        final @Nullable Origin sigOrigin = sdfOrigins[sigOriginOffset];

        // SDF completions
        final Origin sdfCompletionOrigin = sdfCompletionsTasks(input, srcGenSyntaxDir, srcGenSyntaxCompletionDir,
                targetMetaborgDir);

        // SDF meta-module for creating a Stratego concrete syntax extension parse table
        final List<Origin> sdfMetaOrigins = Lists.newArrayList();
        sdfMetaModuleTasks(input, srcGenSyntaxDir, sdfMetaOrigins);

        // Stratego
        strategoTasks(input, targetMetaborgDir, parenthesizeOrigin, sigOrigin, sdfCompletionOrigin,
                sdfMetaOrigins);

        return None.val;
    }

    private Origin[] sdfTasks(GenerateSourcesBuilder.Input input, final File srcGenSigDir, final File srcGenSyntaxDir,
            final File srcGenPpDir, final File targetMetaborgDir) throws IOException {
        final Origin[] sdfOrigins = new Origin[2];
        if (input.sdfModule != null && input.sdfEnabled) {
            final String sdfModule = input.sdfModule;
            final File sdfFile = input.sdfFile;

            // new parse table generator
            if (input.sdf2tableVersion == Sdf2tableVersion.java || input.sdf2tableVersion == Sdf2tableVersion.dynamic
                    || input.sdf2tableVersion == Sdf2tableVersion.incremental) {
                // Get JSGLR parse table from the normalized SDF aterm
                final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic
                        || input.sdf2tableVersion == Sdf2tableVersion.incremental);
                final boolean dataDependent = (input.jsglrVersion == JSGLRVersion.dataDependent);
                final boolean layoutSensitive = (input.jsglrVersion == JSGLRVersion.layoutSensitive);
                final File srcNormDir = toFile(paths.syntaxNormDir());
                final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf.tbl");
                final File contextualGrammarFile = FileUtils.getFile(targetMetaborgDir, "ctxgrammar.aterm");
                final File persistedTableFile = FileUtils.getFile(targetMetaborgDir, "table.bin");
                final File sdfNormFile = FileUtils.getFile(srcNormDir, sdfModule + "-norm.aterm");
                final List<String> paths = Lists.newLinkedList();
                paths.add(srcGenSyntaxDir.getAbsolutePath());

                for (LanguageIdentifier langId : input.sourceDeps) {
                    ILanguageImpl lang = context.languageService().getImpl(langId);
                    for (final ILanguageComponent component : lang.components()) {
                        ILanguageComponentConfig config = component.config();
                        Collection<IExportConfig> exports = config.exports();
                        for (IExportConfig exportConfig : exports) {
                            exportConfig.accept(new IExportVisitor() {
                                @Override
                                public void visit(LangDirExport export) {
                                    if (export.language.equals(SpoofaxConstants.LANG_ATERM_NAME)) {
                                        try {
                                            paths.add(
                                                    toFileReplicate(component.location().resolveFile(export.directory))
                                                            .getAbsolutePath());
                                        } catch (FileSystemException e) {
                                            System.out.println("Failed to locate path");
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void visit(LangFileExport export) {
                                    // Ignore file exports
                                }

                                @Override
                                public void visit(ResourceExport export) {
                                    // Ignore resource exports

                                }
                            });
                        }
                    }
                }

                final Origin sdf2TableJavaOrigin = Sdf2Table
                        .origin(new Sdf2Table.Input(context, sdfNormFile, tableFile, persistedTableFile,
                                contextualGrammarFile, paths, dynamicGeneration, dataDependent, layoutSensitive));

                requireBuild(sdf2TableJavaOrigin);

                // New parenthesizer
                final File parenthesizerFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
                sdfOrigins[parenthesizeOriginOffset] = Sdf2Parenthesize
                        .origin(new Sdf2Parenthesize.Input(context, persistedTableFile, parenthesizerFile, sdfModule));
            } else {

                // Get the SDF def file, either from existing external def, or by running pack
                // SDF on the grammar
                // specification.
                final @Nullable File packSdfFile;
                final @Nullable Origin packSdfOrigin;
                if (input.sdfExternalDef != null) {
                    packSdfFile = input.sdfExternalDef;
                    packSdfOrigin = null;
                } else if (sdfFile != null) {
                    require(sdfFile, FileExistsStamper.instance);
                    if (!sdfFile.exists()) {
                        throw new IOException("Main SDF file at " + sdfFile + " does not exist");
                    }

                    packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + ".def");
                    packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfModule, sdfFile, packSdfFile,
                            input.packSdfIncludePaths, input.packSdfArgs, null));
                } else {
                    packSdfFile = null;
                    packSdfOrigin = null;
                }

                if (packSdfFile != null) {
                    // Get Stratego signatures file when using an external def, or when using sdf2,
                    // from the SDF def
                    // file.
                    if (input.sdfExternalDef != null || input.sdfVersion == SdfVersion.sdf2) {
                        final File rtgFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".rtg");
                        final Origin rtgOrigin = Sdf2Rtg
                                .origin(new Sdf2Rtg.Input(context, packSdfFile, rtgFile, sdfModule, packSdfOrigin));
                        final File sigFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".str");
                        final String sigModule = "signatures/" + sdfModule;
                        sdfOrigins[sigOriginOffset] = Rtg2Sig
                                .origin(new Rtg2Sig.Input(context, rtgFile, sigFile, sigModule, rtgOrigin));
                    }

                    // Get Stratego parenthesizer file, from the SDF def file.
                    final File parenthesizeFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
                    final String parenthesizeModule = "pp/" + sdfModule + "-parenthesize";
                    sdfOrigins[parenthesizeOriginOffset] = Sdf2ParenthesizeLegacy
                            .origin(new Sdf2ParenthesizeLegacy.Input(context, packSdfFile, parenthesizeFile, sdfModule,
                                    parenthesizeModule, packSdfOrigin));

                    // Get SDF permissive def file, from the SDF def file.
                    final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + "-permissive.def");
                    final Origin permissiveDefOrigin = MakePermissive.origin(new MakePermissive.Input(context,
                            packSdfFile, permissiveDefFile, sdfModule, packSdfOrigin));

                    // Get JSGLR parse table, from the SDF permissive def file.
                    final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf.tbl");
                    final Origin sdf2TableOrigin = Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context,
                            permissiveDefFile, tableFile, sdfModule, permissiveDefOrigin));

                    requireBuild(sdf2TableOrigin);

                }
            }
        }
        return sdfOrigins;
    }

    private Origin sdfCompletionsTasks(GenerateSourcesBuilder.Input input, final File srcGenSyntaxDir,
            final File srcGenSyntaxCompletionDir, final File targetMetaborgDir) throws IOException {
        final Origin sdfCompletionOrigin;
        if (input.sdfCompletionFile != null && input.sdfEnabled) {
            final String sdfCompletionsModule = input.sdfCompletionModule;
            final File sdfCompletionsFile = input.sdfCompletionFile;

            if (input.sdf2tableVersion == Sdf2tableVersion.java || input.sdf2tableVersion == Sdf2tableVersion.dynamic
                    || input.sdf2tableVersion == Sdf2tableVersion.incremental) {
                // Get JSGLR parse table, from the normalized SDF aterm

                final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic
                        || input.sdf2tableVersion == Sdf2tableVersion.incremental);
                final boolean dataDependent = (input.jsglrVersion == JSGLRVersion.dataDependent);
                final boolean layoutSensitive = (input.jsglrVersion == JSGLRVersion.layoutSensitive);
                final File persistedTableFile = FileUtils.getFile(targetMetaborgDir, "table-completions.bin");
                final List<String> paths = Lists.newLinkedList();
                paths.add(srcGenSyntaxDir.getAbsolutePath());

                for (LanguageIdentifier langId : input.sourceDeps) {
                    ILanguageImpl lang = context.languageService().getImpl(langId);
                    for (final ILanguageComponent component : lang.components()) {
                        ILanguageComponentConfig config = component.config();
                        Collection<IExportConfig> exports = config.exports();
                        for (IExportConfig exportConfig : exports) {
                            exportConfig.accept(new IExportVisitor() {
                                @Override
                                public void visit(LangDirExport export) {
                                    if (export.language.equals(SpoofaxConstants.LANG_ATERM_NAME)) {
                                        try {
                                            paths.add(
                                                    toFileReplicate(component.location().resolveFile(export.directory))
                                                            .getAbsolutePath());
                                        } catch (FileSystemException e) {
                                            System.out.println("Failed to locate path");
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void visit(LangFileExport export) {
                                    // Ignore file exports
                                }

                                @Override
                                public void visit(ResourceExport export) {
                                    // Ignore resource exports

                                }
                            });
                        }
                    }
                }

                final File tableFile = FileUtils.getFile(targetMetaborgDir, "sdf-completions.tbl");
                sdfCompletionOrigin = Sdf2Table.origin(new Sdf2Table.Input(context, sdfCompletionsFile, tableFile,
                        persistedTableFile, null, paths, dynamicGeneration, dataDependent, layoutSensitive));

                requireBuild(sdfCompletionOrigin);
            } else {

                // Get the SDF def file, either from existing external def, or by running pack
                // SDF on the grammar
                // specification.
                final @Nullable File packSdfCompletionsFile;
                final @Nullable Origin packSdfCompletionsOrigin;
                if (sdfCompletionsFile != null) {
                    require(sdfCompletionsFile, FileExistsStamper.instance);
                    if (!sdfCompletionsFile.exists()) {
                        throw new IOException("Main SDF completions file at " + sdfCompletionsFile + " does not exist");
                    }

                    packSdfCompletionsFile = FileUtils.getFile(srcGenSyntaxCompletionDir,
                            sdfCompletionsModule + ".def");
                    packSdfCompletionsOrigin = PackSdf
                            .origin(new PackSdf.Input(context, sdfCompletionsModule, sdfCompletionsFile,
                                    packSdfCompletionsFile, input.packSdfIncludePaths, input.packSdfArgs, null));
                } else {
                    packSdfCompletionsFile = null;
                    packSdfCompletionsOrigin = null;
                }

                if (packSdfCompletionsFile != null) {
                    // Get SDF permissive def file, from the SDF def file.
                    final File permissiveCompletionsDefFile = FileUtils.getFile(srcGenSyntaxCompletionDir,
                            sdfCompletionsModule + "-permissive.def");
                    final Origin permissiveCompletionsDefOrigin = MakePermissive
                            .origin(new MakePermissive.Input(context, packSdfCompletionsFile,
                                    permissiveCompletionsDefFile, sdfCompletionsModule, packSdfCompletionsOrigin));

                    // Get JSGLR parse table, from the SDF permissive def file.
                    final File completionsTableFile = FileUtils.getFile(targetMetaborgDir, "sdf-completions.tbl");
                    sdfCompletionOrigin = Sdf2TableLegacy.origin(
                            new Sdf2TableLegacy.Input(context, permissiveCompletionsDefFile, completionsTableFile,
                                    "completion/" + sdfCompletionsModule, permissiveCompletionsDefOrigin));

                    requireBuild(sdfCompletionOrigin);
                } else {
                    sdfCompletionOrigin = null;
                }
            }
        } else {
            sdfCompletionOrigin = null;
        }
        return sdfCompletionOrigin;
    }

    private void sdfMetaModuleTasks(GenerateSourcesBuilder.Input input, final File srcGenSyntaxDir,
            final List<Origin> sdfMetaOrigins) throws IOException {
        for (int i = 0; i < input.sdfMetaFiles.size(); i++) {
            final File sdfMetaFile = input.sdfMetaFiles.get(i);

            if (sdfMetaFile != null) {
                require(sdfMetaFile, FileExistsStamper.instance);
                if (!sdfMetaFile.exists()) {
                    throw new IOException("Main meta-SDF file at " + sdfMetaFile + " does not exist");
                }
                final String sdfMetaModule = input.sdfMetaModules.get(i);

                final BuildRequest<GetStrategoMix.Input, OutputPersisted<File>, GetStrategoMix, SpoofaxBuilderFactory<GetStrategoMix.Input, OutputPersisted<File>, GetStrategoMix>> getStrategoMixRequest = GetStrategoMix
                        .request(new GetStrategoMix.Input(context));
                final File strategoMixFile = requireBuild(getStrategoMixRequest).val();
                final Origin strategoMixOrigin = Origin.from(getStrategoMixRequest);
                final Arguments packSdfMetaArgs = new Arguments(input.packSdfArgs);
                packSdfMetaArgs.addFile("-Idef", strategoMixFile);

                final File packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + ".def");

                final Origin packSdfOrigin = PackSdf.origin(new PackSdf.Input(context, sdfMetaModule, sdfMetaFile,
                        packSdfFile, input.packSdfIncludePaths, packSdfMetaArgs, strategoMixOrigin));

                final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + "-permissive.def");
                final Origin permissiveDefOrigin = MakePermissive.origin(new MakePermissive.Input(context, packSdfFile,
                        permissiveDefFile, sdfMetaModule, packSdfOrigin));

                final File transDir = toFile(paths.transDir());
                final File tableFile = FileUtils.getFile(transDir, sdfMetaModule + ".tbl");
                sdfMetaOrigins.add(Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context, permissiveDefFile,
                        tableFile, sdfMetaModule, permissiveDefOrigin)));
                requireBuild(sdfMetaOrigins.get(i));
            }
        }
    }

    private void strategoTasks(GenerateSourcesBuilder.Input input, final File targetMetaborgDir,
            final Origin parenthesizeOrigin, final Origin sigOrigin, final Origin sdfCompletionOrigin,
            final List<Origin> sdfMetaOrigins) throws IOException {
        final File strFile = input.strFile;
        if (strFile != null) {
            require(strFile, FileExistsStamper.instance);
            if (!strFile.exists()) {
                throw new IOException("Main Stratego file at " + strFile + " does not exist");
            }

            boolean buildStrJavaStrat = input.strJavaStratPackage != null && input.strJavaStratFile != null;
            if (buildStrJavaStrat) {
                require(input.strJavaStratFile, FileExistsStamper.instance);
                if (!input.strJavaStratFile.exists()) {
                    throw new IOException(
                            "Main Stratego Java strategies file at " + input.strJavaStratFile + " does not exist");
                }
            }

            final Arguments extraArgs = new Arguments();
            extraArgs.addAll(input.strjArgs);

            final File outputFile;
            final File depPath;
            if (input.strFormat == StrategoFormat.ctree) {
                outputFile = FileUtils.getFile(targetMetaborgDir, "stratego.ctree");
                depPath = outputFile;
                extraArgs.add("-F");
            } else {
                depPath = toFile(paths.strSrcGenJavaTransDir(input.languageId));
                outputFile = FileUtils.getFile(depPath, "Main.java");
                extraArgs.add("-la", "java-front");
                if (buildStrJavaStrat) {
                    extraArgs.add("-la", input.strJavaStratPackage);
                }
            }

            if (input.strExternalJarFlags != null) {
                extraArgs.addLine(input.strExternalJarFlags);
            }

            final Origin origin;

            build.pluto.dependency.Origin.Builder builder = Origin.Builder();

            builder = builder.add(parenthesizeOrigin).add(sigOrigin).add(sdfCompletionOrigin);

            for (Origin sdfMetaOrigin : sdfMetaOrigins) {
                builder = builder.add(sdfMetaOrigin);
            }

            origin = builder.get();

            final File cacheDir = toFile(paths.strCacheDir());

            switch(input.strBuildSetting) {
            case batch:
                strategoBatchTasks(input, strFile, extraArgs, outputFile, depPath, origin, cacheDir);
                break;
            case incremental:
                strategoIncrCompTasks(input, cacheDir, extraArgs, origin);
                break;
            default:
                throw new IOException("Stratego build setting has unexpected value: " + input.strBuildSetting.toString());
            }
        }
    }

    private void strategoBatchTasks(GenerateSourcesBuilder.Input input, final File strFile, final Arguments extraArgs,
            final File outputFile, final File depPath, final Origin origin, final File cacheDir) throws IOException {
        final Strj.Input strjInput =

                new Strj.Input(context, strFile, outputFile, depPath, input.strJavaPackage, true, true,
                        input.strjIncludeDirs, input.strjIncludeFiles, Lists.newArrayList(), cacheDir, extraArgs,
                        origin);

        final Origin strjOrigin = Strj.origin(strjInput);
        requireBuild(strjOrigin);

        // Typesmart
        final File typesmartExportedFile = toFile(paths.strTypesmartExportedFile());
        final Typesmart.Input typesmartInput = new Typesmart.Input(context, input.strFile, input.strjIncludeDirs,
                typesmartExportedFile, origin);
        final Origin typesmartOrigin = Typesmart.origin(typesmartInput);
        requireBuild(typesmartOrigin);
    }

    private void strategoIncrCompTasks(GenerateSourcesBuilder.Input input, File cacheDir, Arguments extraArgs, Origin origin)
            throws IOException {
        // TODO: use mainFile once we track imports
        @SuppressWarnings("unused")
        File mainFile = input.strFile;

        // FRONTEND
        List<File> boilerplateFiles = Lists.newArrayList();
        Origin.Builder allFrontEndTasks = Origin.Builder();
        Map<String, List<StrategoIncrementalFrontEnd.Output>> strategies = Maps.newHashMap();
        for (File strFile : FileUtils.listFiles(context.baseDir, new String[] { "str" }, true)) {
            StrategoIncrementalFrontEnd.Input frontEndInput = new StrategoIncrementalFrontEnd.Input(context, strFile,
                    input.strJavaPackage, cacheDir, extraArgs, origin);
            StrategoIncrementalFrontEnd.Output frontEndOutput = requireBuild(
                    StrategoIncrementalFrontEnd.request(frontEndInput));
            // shuffling output for backend
            allFrontEndTasks.add(frontEndOutput.request);
            boilerplateFiles
                    .add(new File(context.baseDir, "src-gen/stratego_sugar/" + frontEndOutput.moduleName + ".aterm"));
            for (String strategy : frontEndOutput.seenStrategies) {
                strategies.computeIfAbsent(strategy, k -> Lists.newArrayList());
                strategies.get(strategy).add(frontEndOutput);
            }
        }

        // BACKEND
        for (Map.Entry<String, List<StrategoIncrementalFrontEnd.Output>> entry : strategies.entrySet()) {
            Origin.Builder originBuilder = Origin.Builder();
            List<File> contributions = Lists.newArrayList();
            String strategyName = entry.getKey();
            for (StrategoIncrementalFrontEnd.Output output : entry.getValue()) {
                originBuilder.add(output.request);
                contributions.add(new File(context.baseDir,
                        "src-gen/stratego_sugar/" + strategyName + "/" + output.moduleName + ".aterm"));
            }
            StrategoIncrementalBackEnd.Input backEndInput = new StrategoIncrementalBackEnd.Input(context,
                    originBuilder.get(), strategyName, contributions, false);
            requireBuild(StrategoIncrementalBackEnd.request(backEndInput));
        }
        // boilerplate task
        StrategoIncrementalBackEnd.Input backEndInput = new StrategoIncrementalBackEnd.Input(context,
                allFrontEndTasks.get(), null, boilerplateFiles, true);
    }
}
