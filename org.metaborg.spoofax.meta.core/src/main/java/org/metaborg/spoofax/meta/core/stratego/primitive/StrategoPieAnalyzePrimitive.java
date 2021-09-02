package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.SpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.IPieProvider;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.api.ValueSupplier;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.task.CheckOpenModule;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.output.CheckOpenModuleOutput;
import mb.stratego.build.util.LastModified;

public class StrategoPieAnalyzePrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoPieAnalyzePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;
    @Inject private static Provider<ILanguageService> languageServiceProvider;
    @Inject private static Provider<IPieProvider> pieProviderProvider;

    // Using provider to break cycle between Check -> Stratego runtime -> all primitives -> this primitive
    private final Provider<CheckOpenModule> checkModuleProvider;
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;

    @Inject public StrategoPieAnalyzePrimitive(Provider<CheckOpenModule> checkModuleProvider, ILanguagePathService languagePathService,
        IResourceService resourceService) {
        super("stratego_pie_analyze", 0, 0);
        this.checkModuleProvider = checkModuleProvider;
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
    }

    @Override protected @Nullable IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final IStrategoAppl ast = TermUtils.toApplAt(current, 0);
        final String path = TermUtils.toJavaStringAt(current, 1);
//        final String projectPath = TermUtils.toJavaStringAt(current, 2);

        final String moduleName;
        if(!(ast.getName().equals("Module") && ast.getSubtermCount() == 2)) {
            moduleName = path;
        } else {
            moduleName = TermUtils.toJavaStringAt(ast, 0);
        }

        final IProject project = context.project();
        if(project == null) {
            logger.warn("Cannot find project for opened file, cancelling analysis. ");
            return null;
        }

        if(languageSpecServiceProvider == null || languageServiceProvider == null || pieProviderProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Spoofax meta services is not available; static injection failed");
            return null;
        }

        final @Nullable ISpoofaxLanguageSpec languageSpec = getLanguageSpecification(project);
        if(languageSpec == null) {
            logger.warn("Cannot find language specification for project of opened file, cancelling analysis. ");
            return null;
        }

        final ISpoofaxLanguageSpecConfig config = languageSpec.config();

        final FileObject baseLoc = languageSpec.location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);

        String strMainModule = NameUtil.toJavaId(config.strategoName().toLowerCase());

        final Iterable<FileObject> strRoots =
            languagePathService.sourcePaths(project, SpoofaxConstants.LANG_STRATEGO_NAME);
        @Nullable File strMainFile;
        final FileObject strMainFileCandidate =
            config.strVersion().findStrMainFile(paths, strRoots, config.strategoName());
        if(strMainFileCandidate != null && strMainFileCandidate.exists()) {
            strMainFile = resourceService.localPath(strMainFileCandidate);
            if(strMainFile == null || !strMainFile.exists()) {
                logger.info("Main Stratego2 file at " + strMainFile + " does not exist");
                strMainFile = resourceService.localFile(resourceService.resolve(baseLoc, path));
                strMainModule = moduleName;
            }
        } else {
            logger.info("Main Stratego2 file with name " + strMainModule + ".str2 does not exist");
            strMainFile = resourceService.localFile(resourceService.resolve(baseLoc, path));
            strMainModule = moduleName;
        }
        final File strFile = resourceService.localFile(resourceService.resolve(baseLoc, path));

        final @Nullable String strExternalJarFlags = config.strExternalJarFlags();

        final Iterable<FileObject> strIncludePaths = Iterables.concat(
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME),
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO2_NAME));
        final FileObject strjIncludesReplicateDir = paths.replicateDir().resolveFile("strj-includes");
        strjIncludesReplicateDir.delete(new AllFileSelector());
        final ArrayList<ResourcePath> strjIncludeDirs = new ArrayList<>();
        for(FileObject strIncludePath : strIncludePaths) {
            if(!strIncludePath.exists()) {
                continue;
            }
            if(strIncludePath.isFolder()) {
                strjIncludeDirs.add(new FSPath(resourceService.localFile(strIncludePath, strjIncludesReplicateDir)));
            }
        }

        final Arguments extraArgs = new Arguments();
        extraArgs.addAll(config.strArgs());

        extraArgs.add("-la", "java-front");

        if(strExternalJarFlags != null) {
            extraArgs.addLine(strExternalJarFlags);
        }

        final File projectLocation = resourceService.localPath(paths.root());
        assert projectLocation != null;
        final ResourcePath projectPath = new FSPath(projectLocation);

        final ArrayList<Supplier<Stratego2LibInfo>> str2libraries = new ArrayList<>();
        for(LanguageIdentifier sourceDep : config.sourceDeps()) {
            final @Nullable ILanguageImpl sourceDepImpl = languageServiceProvider.get().getImpl(sourceDep);
            if(sourceDepImpl == null) {
                continue;
            }
            for(ILanguageComponent sourceDepImplComp : sourceDepImpl.components()) {
                final String[] str2libProject = { null };
                for(IExportConfig export : sourceDepImplComp.config().exports()) {
                    if(str2libProject[0] != null) {
                        break;
                    }
                    export.accept(new IExportVisitor() {
                        @Override public void visit(LangDirExport resource) {}

                        @Override public void visit(LangFileExport resource) {
                            if(resource.language.equals("StrategoLang") && resource.file.endsWith("str2lib")) {
                                str2libProject[0] = resource.file;
                            }
                        }

                        @Override public void visit(ResourceExport resource) {}
                    });
                }
                if(str2libProject[0] != null) {
                    final ResourcePath str2LibFile = new FSPath(resourceService
                        .localFile(sourceDepImplComp.location().resolveFile(str2libProject[0]),
                            paths.replicateDir().resolveFile("strj-includes")));
                    final @Nullable DynamicClassLoadingFacet facet =
                        sourceDepImplComp.facet(DynamicClassLoadingFacet.class);
                    if(facet == null) {
                        continue;
                    }
                    final ArrayList<ResourcePath> jarFiles =
                        new ArrayList<>(facet.jarFiles.size());
                    for(FileObject file : facet.jarFiles) {
                        jarFiles.add(new FSPath(resourceService.localFile(file, paths.replicateDir().resolveFile("str2-includes"))));
                    }
                    str2libraries.add(new ValueSupplier<>(new Stratego2LibInfo(str2LibFile, jarFiles)));
                }
            }
        }

        final ArrayList<STask<?>> sdfTasks = new ArrayList<>(0);

        // Gather all Stratego files to be checked for changes
        final Set<Path> changedFiles = GenerateSourcesBuilder.getChangedFiles(projectLocation);
        final Set<ResourceKey> changedResources = new HashSet<>(changedFiles.size() * 2);
        for(Path changedFile : changedFiles) {
            changedResources.add(new FSPath(changedFile));
        }

        final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>();
        GenerateSourcesBuilder.splitOffLinkedLibrariesIncludeDirs(extraArgs, linkedLibraries, strjIncludeDirs, projectLocation.getPath());
        final LastModified<IStrategoTerm> astWLM =
            new LastModified<>(ast, Instant.now().getEpochSecond());
        final boolean isLibrary = false;
        final ModuleIdentifier moduleIdentifier =
            new ModuleIdentifier(strFile.getName().endsWith(".str"), isLibrary, moduleName, new FSPath(strFile));
        final ModuleIdentifier mainModuleIdentifier =
            new ModuleIdentifier(strMainFile.getName().endsWith(".str"), isLibrary, strMainModule, new FSPath(strMainFile));
        final boolean autoImportStd = false;

        final IModuleImportService.ImportResolutionInfo importResolutionInfo =
            new IModuleImportService.ImportResolutionInfo(sdfTasks, strjIncludeDirs,
                linkedLibraries, str2libraries);
        final CheckModuleInput checkModuleInput = new CheckModuleInput(
            new FrontInput.FileOpenInEditor(moduleIdentifier, importResolutionInfo, astWLM,
                autoImportStd), mainModuleIdentifier, projectPath);
        final Task<CheckOpenModuleOutput> checkModuleTask = checkModuleProvider.get().createTask(checkModuleInput);

        final IPieProvider pieProvider = pieProviderProvider.get();
        final Pie pie = pieProvider.pie();

        final IStrategoList.Builder errors = B.listBuilder();
        final IStrategoList.Builder warnings = B.listBuilder();
        final IStrategoList.Builder notes = B.listBuilder();
        final CheckOpenModuleOutput analysisInformation;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(pie) {
            GenerateSourcesBuilder.initCompiler(pieProvider, checkModuleTask);
            try(final MixedSession session = pie.newSession()) {
                TopDownSession tdSession = session.updateAffectedBy(changedResources);
                session.deleteUnobservedTasks(t -> true, (t, r) -> {
                    if(Objects.equals(r.getLeafExtension(), "java")) {
                        logger.debug("Deleting garbage from previous build: " + r);
                        return true;
                    }
                    return false;
                });
                analysisInformation = tdSession.getOutput(checkModuleTask);
            } catch(ExecException e) {
                logger.warn("Incremental Stratego build failed", e);
                return null;
            } catch(InterruptedException e) {
                // Ignore
                return null;
            }
        }

        for(Message message : analysisInformation.messages) {
            final IStrategoString term = factory.makeString(message.locationTermString);
            if(message.filename == null) {
                logger.debug("No origins for message: " + message);
            } else {
                assert message.sourceRegion != null; // otherwise filename would have also been null
                final ImploderAttachment imploderAttachment = ImploderAttachment
                    .createCompactPositionAttachment(message.filename, message.sourceRegion.startRow, message.sourceRegion.startColumn,
                        message.sourceRegion.startOffset, message.sourceRegion.endOffset);
                term.putAttachment(imploderAttachment);
            }
            final IStrategoTuple messageTuple = B.tuple(term, B.string(message.getMessage()));
            switch(message.severity) {
                case ERROR:
                    errors.add(messageTuple);
                    break;
                case NOTE:
                    notes.add(messageTuple);
                    break;
                case WARNING:
                    warnings.add(messageTuple);
                    break;
            }
        }

        return B.tuple(analysisInformation.astWithCasts, B.list(errors), B.list(warnings), B.list(notes));
    }

    private @Nullable ISpoofaxLanguageSpec getLanguageSpecification(IProject project) throws MetaborgException {
        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return null;
        }
        final ISpoofaxLanguageSpecService languageSpecService = languageSpecServiceProvider.get();
        if(!languageSpecService.available(project)) {
            logger.error("Language specification service is not available for " + project);
            return null;
        }
        final ISpoofaxLanguageSpec languageSpec;
        try {
            languageSpec = languageSpecService.get(project);
        } catch(ConfigException e) {
            throw new MetaborgException("Unable to get language specification configuration for " + project, e);
        }
        return languageSpec;
    }

    @Override public void close() throws Exception {
        pieProviderProvider = null;
        languageSpecServiceProvider = null;
        languageServiceProvider = null;
    }
}
