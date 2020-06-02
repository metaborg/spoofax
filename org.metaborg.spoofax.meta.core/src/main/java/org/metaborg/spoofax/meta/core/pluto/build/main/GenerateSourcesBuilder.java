package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.metaborg.spoofax.meta.core.config.SdfVersion;
import org.metaborg.spoofax.meta.core.config.StrategoBuildSetting;
import org.metaborg.spoofax.meta.core.config.StrategoFormat;
import org.metaborg.spoofax.meta.core.config.StrategoGradualSetting;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.MakePermissive;
import org.metaborg.spoofax.meta.core.pluto.build.PackSdfLegacy;
import org.metaborg.spoofax.meta.core.pluto.build.Rtg2Sig;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Parenthesize;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2ParenthesizeLegacy;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2TableLegacy;
import org.metaborg.spoofax.meta.core.pluto.build.Strj;
import org.metaborg.spoofax.meta.core.pluto.build.misc.GetStrategoMix;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.FileHashStamper;
import mb.pie.api.ExecException;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.stratego.build.strincr.BuildStats;
import mb.stratego.build.strincr.StrIncr;

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

        public final @Nullable Boolean checkOverlap;
        public final @Nullable Boolean checkPriorities;

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
        public final StrategoGradualSetting strGradualSetting;


        public Input(SpoofaxContext context, String languageId, Collection<LanguageIdentifier> sourceDeps,
            @Nullable Boolean sdfEnabled, @Nullable String sdfModule, @Nullable File sdfFile, JSGLRVersion jsglrVersion,
            SdfVersion sdfVersion, Sdf2tableVersion sdf2tableVersion, @Nullable Boolean checkOverlap,
            @Nullable Boolean checkPriorities, @Nullable File sdfExternalDef, List<File> packSdfIncludePaths,
            Arguments packSdfArgs, @Nullable String sdfCompletionModule, @Nullable File sdfCompletionFile,
            @Nullable List<String> sdfMetaModules, @Nullable List<File> sdfMetaFiles, @Nullable File strFile,
            @Nullable String strJavaPackage, @Nullable String strJavaStratPackage, @Nullable File strJavaStratFile,
            StrategoFormat strFormat, @Nullable File strExternalJar, @Nullable String strExternalJarFlags,
            List<File> strjIncludeDirs, List<File> strjIncludeFiles, Arguments strjArgs,
            StrategoBuildSetting strBuildSetting, StrategoGradualSetting strGradualSetting) {
            super(context);
            this.languageId = languageId;
            this.sdfEnabled = sdfEnabled;
            this.sourceDeps = sourceDeps;
            this.sdfModule = sdfModule;
            this.sdfFile = sdfFile;
            this.jsglrVersion = jsglrVersion;
            this.sdfVersion = sdfVersion;
            this.sdf2tableVersion = sdf2tableVersion;
            this.checkOverlap = checkOverlap;
            this.checkPriorities = checkPriorities;
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
            this.strGradualSetting = strGradualSetting;
        }
    }

    public static SpoofaxBuilderFactory<Input, None, GenerateSourcesBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(GenerateSourcesBuilder.class, Input.class);

    private static final Set<String> BUILTIN_LIBS = new HashSet<>(Arrays.asList("stratego-lib", "stratego-sglr",
        "stratego-gpp", "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc", "java-front"));
    private static final ILogger logger = LoggerUtils.logger(GenerateSourcesBuilder.class);

    private static Pie pie;


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


    @Override public None build(GenerateSourcesBuilder.Input input)
        throws IOException, MetaborgException {
        // SDF
        Origin.Builder sdfOriginBuilder = Origin.Builder();

        buildSdf(input, sdfOriginBuilder);
        buildSdfMeta(input, sdfOriginBuilder); // SDF meta-module for creating a Stratego concrete syntax extension
                                               // parse table

        final Origin sdfOrigin = sdfOriginBuilder.get();

        // Stratego
        buildStratego(input, sdfOrigin);

        return None.val;
    }

    private void buildSdf(GenerateSourcesBuilder.Input input, Origin.Builder sdfOriginBuilder) throws IOException {
        if(input.sdfModule != null && input.sdfEnabled) {
            if(input.sdf2tableVersion == Sdf2tableVersion.java || input.sdf2tableVersion == Sdf2tableVersion.dynamic
                || input.sdf2tableVersion == Sdf2tableVersion.incremental) {
                newParseTableGenerationBuild(input, sdfOriginBuilder);
            } else {
                oldParseTableGenerationBuild(input, sdfOriginBuilder);
            }
        }
    }

    private void newParseTableGenerationBuild(GenerateSourcesBuilder.Input input, Origin.Builder sdfOriginBuilder)
        throws IOException {
        // Standard parser generation
        final File srcNormDir = toFile(paths.syntaxNormDir());
        final File sdfNormFile = FileUtils.getFile(srcNormDir, input.sdfModule + "-norm.aterm");

        final BuildRequest<?, OutputPersisted<File>, ?, ?> parseTableGeneration =
            newParseTableGeneration(input, sdfNormFile, "sdf.tbl", "table.bin", false);

        sdfOriginBuilder.add(parseTableGeneration);
        requireBuild(parseTableGeneration);

        // Generate parenthesizer
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());
        final File parenthesizerOutputFile = FileUtils.getFile(srcGenPpDir, input.sdfModule + "-parenthesize.str");

        Sdf2Parenthesize.Input parenthesizeInput =
            new Sdf2Parenthesize.Input(context, parseTableGeneration, input.sdfModule, parenthesizerOutputFile);
        final BuildRequest<?, ?, ?, ?> parenthesize = Sdf2Parenthesize.request(parenthesizeInput);

        sdfOriginBuilder.add(parenthesize);
        requireBuild(parenthesize);

        // Parser generation for completions
        if(input.sdfCompletionFile != null && input.sdfEnabled) {
            final BuildRequest<?, ?, ?, ?> parseTableGenerationCompletions = newParseTableGeneration(input,
                input.sdfCompletionFile, "sdf-completions.tbl", "table-completions.bin", true);

            sdfOriginBuilder.add(parseTableGenerationCompletions);
            requireBuild(parseTableGenerationCompletions);
        }
    }

    private BuildRequest<?, OutputPersisted<File>, ?, ?> newParseTableGeneration(GenerateSourcesBuilder.Input input,
        File sdfNormFile, String tableFilename, String persistedTableFilename, boolean isCompletions)
        throws IOException {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
        final File tableFile = FileUtils.getFile(targetMetaborgDir, tableFilename);
        final File persistedTableFile = FileUtils.getFile(targetMetaborgDir, persistedTableFilename);

        final boolean dynamicGeneration = (input.sdf2tableVersion == Sdf2tableVersion.dynamic
            || input.sdf2tableVersion == Sdf2tableVersion.incremental);
        final boolean dataDependent = (input.jsglrVersion == JSGLRVersion.dataDependent);
        final boolean layoutSensitive = (input.jsglrVersion == JSGLRVersion.layoutSensitive);
        final boolean checkOverlap = input.checkOverlap;
        final boolean checkPriorities = input.checkPriorities;
        ParseTableConfiguration config = new ParseTableConfiguration(dynamicGeneration, dataDependent, !layoutSensitive,
            checkOverlap, checkPriorities);

        Sdf2Table.Input sdf2TableInput = new Sdf2Table.Input(context, sdfNormFile, input.sourceDeps, tableFile,
            persistedTableFile, config, isCompletions);

        return Sdf2Table.request(sdf2TableInput);
    }


    private void oldParseTableGenerationBuild(GenerateSourcesBuilder.Input input, Origin.Builder sdfOriginBuilder)
        throws IOException {
        File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());

        // Packing normalized .sdf files in a single .def file
        PackSdfBuild packSdfBuild =
            oldParseTableGenerationPack(input, srcGenSyntaxDir, input.sdfModule, input.sdfFile, input.sdfExternalDef);

        if(packSdfBuild.file != null) {
            // Get Stratego signatures file when using an external .def, or when using sdf2, from the SDF .def file
            if(input.sdfExternalDef != null || input.sdfVersion == SdfVersion.sdf2) {
                final Origin sigOrigin = oldParseTableGenerationSignatures(input, sdfOriginBuilder, packSdfBuild,
                    srcGenSyntaxDir, input.sdfModule, input.sdfExternalDef);

                sdfOriginBuilder.add(sigOrigin);
            }

            // Get Stratego parenthesizer file, from the SDF .def file
            Origin parenthesizeOrigin =
                oldParseTableGenerationParenthesize(input, sdfOriginBuilder, packSdfBuild, input.sdfModule);

            sdfOriginBuilder.add(parenthesizeOrigin);

            // Standard parser generation
            MakePermissiveBuild makePermissiveBuild =
                oldParseTableGenerationMakePermissive(packSdfBuild, srcGenSyntaxDir, input.sdfModule);

            final Origin sdfOrigin = oldParseTableGeneration(makePermissiveBuild, input.sdfModule, "sdf.tbl", "");

            requireBuild(sdfOrigin);
        }

        // Again packing, make permissive, and generation for completions parse table
        if(input.sdfCompletionFile != null && input.sdfEnabled) {
            File srcGenSyntaxCompletionsDir = toFile(paths.syntaxCompletionSrcGenDir());

            PackSdfBuild packSdfCompletionsBuild = oldParseTableGenerationPack(input, srcGenSyntaxCompletionsDir,
                input.sdfCompletionModule, input.sdfCompletionFile, null);

            MakePermissiveBuild makePermissiveCompletionsBuild = oldParseTableGenerationMakePermissive(
                packSdfCompletionsBuild, srcGenSyntaxCompletionsDir, input.sdfCompletionModule);

            final Origin sdfCompletionOrigin = oldParseTableGeneration(makePermissiveCompletionsBuild,
                input.sdfCompletionModule, "sdf-completions.tbl", "completion/");

            sdfOriginBuilder.add(sdfCompletionOrigin);
        }
    }

    private PackSdfBuild oldParseTableGenerationPack(GenerateSourcesBuilder.Input input, File srcGenSyntaxDir,
        String sdfModule, File sdfFile, File sdfExternalDef) throws IOException {
        // Get the SDF .def file, either from existing external .def, or by running pack SDF on the grammar
        // specification
        final @Nullable File packSdfFile;
        final @Nullable Origin packSdfOrigin;

        if(sdfExternalDef != null) {
            packSdfFile = sdfExternalDef;
            packSdfOrigin = null;
        } else if(sdfFile != null) {
            require(sdfFile, FileExistsStamper.instance);
            if(!sdfFile.exists()) {
                throw new IOException("Main SDF file at " + sdfFile + " does not exist");
            }

            packSdfFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + ".def");
            packSdfOrigin = PackSdfLegacy.origin(new PackSdfLegacy.Input(context, sdfModule, sdfFile, packSdfFile,
                input.packSdfIncludePaths, input.packSdfArgs, null));
        } else {
            packSdfFile = null;
            packSdfOrigin = null;
        }

        return new PackSdfBuild(packSdfFile, packSdfOrigin);
    }

    private static class PackSdfBuild {

        final @Nullable File file;
        final @Nullable Origin origin;

        PackSdfBuild(File packSdfFile, Origin packSdfOrigin) {
            this.file = packSdfFile;
            this.origin = packSdfOrigin;
        }

    }

    private Origin oldParseTableGenerationSignatures(GenerateSourcesBuilder.Input input,
        Origin.Builder sdfOriginBuilder, PackSdfBuild packSdfBuild, File srcGenSyntaxDir, String sdfModule,
        File sdfExternalDef) {
        final File srcGenSigDir = toFile(paths.syntaxSrcGenSignatureDir());
        final File rtgFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".rtg");
        final Origin rtgOrigin =
            Sdf2Rtg.origin(new Sdf2Rtg.Input(context, packSdfBuild.file, rtgFile, sdfModule, packSdfBuild.origin));

        final File sigFile = FileUtils.getFile(srcGenSigDir, sdfModule + ".str");
        final String sigModule = "signatures/" + sdfModule;
        final Origin sigOrigin = Rtg2Sig.origin(new Rtg2Sig.Input(context, rtgFile, sigFile, sigModule, rtgOrigin));

        return sigOrigin;
    }

    private Origin oldParseTableGenerationParenthesize(GenerateSourcesBuilder.Input input,
        Origin.Builder sdfOriginBuilder, PackSdfBuild packSdfBuild, String sdfModule) {
        final File srcGenPpDir = toFile(paths.syntaxSrcGenPpDir());
        final File parenthesizeFile = FileUtils.getFile(srcGenPpDir, sdfModule + "-parenthesize.str");
        final String parenthesizeModule = "pp/" + sdfModule + "-parenthesize";
        final Origin parenthesizeOrigin = Sdf2ParenthesizeLegacy.origin(new Sdf2ParenthesizeLegacy.Input(context,
            packSdfBuild.file, parenthesizeFile, sdfModule, parenthesizeModule, packSdfBuild.origin));

        return parenthesizeOrigin;
    }

    private MakePermissiveBuild oldParseTableGenerationMakePermissive(PackSdfBuild packSdfBuild, File srcGenSyntaxDir,
        String sdfModule) throws IOException {
        final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfModule + "-permissive.def");
        final Origin permissiveDefOrigin = MakePermissive.origin(
            new MakePermissive.Input(context, packSdfBuild.file, permissiveDefFile, sdfModule, packSdfBuild.origin));

        return new MakePermissiveBuild(permissiveDefFile, permissiveDefOrigin);
    }

    private static class MakePermissiveBuild {

        final @Nullable File file;
        final @Nullable Origin origin;

        MakePermissiveBuild(File permissiveDefFile, Origin permissiveDefOrigin) {
            this.file = permissiveDefFile;
            this.origin = permissiveDefOrigin;
        }

    }

    private Origin oldParseTableGeneration(MakePermissiveBuild makePermissiveBuild, String sdfModule,
        String parseTableFilename, String modulePrefix) throws IOException {
        final File targetMetaborgDir = toFile(paths.targetMetaborgDir());
        final File tableFile = FileUtils.getFile(targetMetaborgDir, parseTableFilename);
        return Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context, makePermissiveBuild.file, tableFile,
            modulePrefix + sdfModule, makePermissiveBuild.origin));
    }

    private void buildSdfMeta(GenerateSourcesBuilder.Input input, Origin.Builder sdfOriginBuilder) throws IOException {
        final File srcGenSyntaxDir = toFile(paths.syntaxSrcGenDir());

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

                final Origin packSdfOrigin = PackSdfLegacy.origin(new PackSdfLegacy.Input(context, sdfMetaModule,
                    sdfMetaFile, packSdfFile, input.packSdfIncludePaths, packSdfMetaArgs, strategoMixOrigin));

                final File permissiveDefFile = FileUtils.getFile(srcGenSyntaxDir, sdfMetaModule + "-permissive.def");
                final Origin permissiveDefOrigin = MakePermissive.origin(
                    new MakePermissive.Input(context, packSdfFile, permissiveDefFile, sdfMetaModule, packSdfOrigin));

                final File transDir = toFile(paths.transDir());
                final File tableFile = FileUtils.getFile(transDir, sdfMetaModule + ".tbl");

                Origin sdfMetaOrigin = Sdf2TableLegacy.origin(new Sdf2TableLegacy.Input(context, permissiveDefFile,
                    tableFile, sdfMetaModule, permissiveDefOrigin));

                sdfOriginBuilder.add(sdfMetaOrigin);

                requireBuild(sdfMetaOrigin);
            }
        }
    }

    private void buildStratego(GenerateSourcesBuilder.Input input, Origin sdfOrigin)
        throws IOException, MetaborgException {
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
            if(input.strFormat == StrategoFormat.ctree && input.strBuildSetting != StrategoBuildSetting.incremental) {
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
                /*
                 * Make sure to require all the sdf stuff before running the stratego compiler which will search for the
                 * generated stratego files.
                 */
                requireBuild(sdfOrigin);
                logger.info("> Compile Stratego code using the incremental compiler");
                final File projectLocation = context.resourceService().localPath(paths.root());
                assert projectLocation != null;

                /*
                 * Make sure Pluto also understands which files Pie will require.
                 */
                final Set<Path> changedFiles = getChangedFiles(projectLocation);
                final Set<ResourceKey> changedResources = new HashSet<>(changedFiles.size() * 2);
                for(Path changedFile : changedFiles) {
                    require(changedFile.toFile(), FileHashStamper.instance);
                    changedResources.add(new FSPath(changedFile));
                }

                final Arguments newArgs = new Arguments();
                final List<String> builtinLibs = splitOffBuiltinLibs(extraArgs, newArgs);
                final StrIncr.Input strIncrInput =
                    new StrIncr.Input(strFile, input.strJavaPackage, input.strjIncludeDirs, builtinLibs, cacheDir,
                        Collections.emptyList(), newArgs, depPath, Collections.emptyList(), projectLocation, input.strGradualSetting == StrategoGradualSetting.on);

                final Pie pie =
                    initCompiler(context.pieProvider(), context.getStrIncrTask().createTask(strIncrInput), depPath);

                BuildStats.reset();
                long totalTime = System.nanoTime();
                try(final PieSession pieSession = pie.newSession()) {
                    pieSession.updateAffectedBy(changedResources);
                    pieSession.deleteUnobservedTasks(t -> true, (t, r) -> {
                        if(r instanceof HierarchicalResource
                            && Objects.equals(((HierarchicalResource) r).getLeafExtension(), "java")) {
                            logger.debug("Deleting garbage from previous build: " + r);
                            return true;
                        }
                        return false;
                    });
                } catch(ExecException e) {
                    throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(), e);
                }
                totalTime = totalTime - System.nanoTime();
            } else {
                final Strj.Input strjInput = new Strj.Input(context, strFile, outputFile, depPath, input.strJavaPackage,
                    true, true, input.strjIncludeDirs, input.strjIncludeFiles, Lists.newArrayList(), cacheDir,
                    extraArgs, sdfOrigin);

                final Origin strjOrigin = Strj.origin(strjInput);
                requireBuild(strjOrigin);
            }
        }
    }

    public static Set<Path> getChangedFiles(File projectLocation) throws IOException {
        final Set<Path> result = new HashSet<>();
        Files.walkFileTree(projectLocation.toPath(), new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                String pathString = path.toString().toLowerCase();
                if(pathString.endsWith(".str") || pathString.endsWith(".rtree") || pathString.endsWith(".ctree")) {
                    result.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    /**
     * Copy oldArgs to newArgs, except for built-in libraries, which are split off and their names returned.
     */
    public static List<String> splitOffBuiltinLibs(Arguments oldArgs, Arguments newArgs) {
        final List<String> builtinLibs = new ArrayList<>();
        for(Iterator<Object> iterator = oldArgs.iterator(); iterator.hasNext();) {
            Object oldArg = iterator.next();
            if(oldArg.equals("-la")) {
                final Object nextOldArg = iterator.next();
                // noinspection SuspiciousMethodCalls
                if(BUILTIN_LIBS.contains(nextOldArg)) {
                    builtinLibs.add((String) nextOldArg);
                } else {
                    newArgs.add(oldArg, nextOldArg);
                }
            } else {
                newArgs.add(oldArg);
            }
        }
        return builtinLibs;
    }

    public static Pie initCompiler(IPieProvider pieProvider, Task<?> strIncrTask)
        throws MetaborgException {
        return initCompiler(pieProvider, strIncrTask, null);
    }

    public static Pie initCompiler(IPieProvider pieProvider, Task<?> strIncrTask, @Nullable File outputPath)
        throws MetaborgException {
        pie = pieProvider.pie();
        if(!pie.hasBeenExecuted(strIncrTask)) {
            BuildStats.reset();
            logger.info("> Clean build required by PIE");
            if(outputPath != null && outputPath.exists()) {
                try {
                    FileUtils.deleteDirectory(outputPath);
                    Files.createDirectories(outputPath.toPath());
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            pieProvider.setLogLevelWarn();
            try(final PieSession session = pie.newSession()) {
                session.require(strIncrTask);
            } catch(ExecException e) {
                throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(), e);
            }
            pieProvider.setLogLevelTrace();
        }
        return pie;
    }

    public static void clean() {
        if(pie != null) {
            pie.dropStore();
            pie = null;
        }
    }
}
