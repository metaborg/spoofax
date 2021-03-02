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
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.NameUtil;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.StrategoBuildSetting;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.type.TypeMessage;
import mb.stratego.build.strincr.task.CheckModule;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrategoGradualSetting;

public class StrategoPieAnalyzePrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoPieAnalyzePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;
    @Inject private static Provider<IPieProvider> pieProviderProvider;

    // Using provider to break cycle between Check -> Stratego runtime -> all primitives -> this primitive
    private final Provider<CheckModule> checkModuleProvider;
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;

    @Inject public StrategoPieAnalyzePrimitive(Provider<CheckModule> checkModuleProvider, ILanguagePathService languagePathService,
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
        @SuppressWarnings("unused") final String projectPath = TermUtils.toJavaStringAt(current, 2);

        if(!(ast.getName().equals("Module") && ast.getSubtermCount() == 2)) {
            throw new MetaborgException("Input AST for Stratego analysis not Module/2.");
        }
        final String moduleName = TermUtils.toJavaStringAt(ast, 0);

        final IProject project = context.project();
        if(project == null) {
            logger.warn("Cannot find project for opened file, cancelling analysis. ");
            return null;
        }

        if(languageSpecServiceProvider == null || pieProviderProvider == null) {
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

        // Fail this primitive if compilation is set to batch mode
        if(config.strBuildSetting() == StrategoBuildSetting.batch) {
            logger.debug("Compilation mode is set to batch, default to old Stratego editor analysis. ");
            return null;
        }

        final FileObject baseLoc = languageSpec.location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);

        final String strModule = config.strategoName();

        final Iterable<FileObject> strRoots =
            languagePathService.sourcePaths(project, SpoofaxConstants.LANG_STRATEGO_NAME);
        final @Nullable File strFile;
        final FileObject strFileCandidate = paths.findStrMainFile(strRoots, strModule);
        if(strFileCandidate != null && strFileCandidate.exists()) {
            strFile = resourceService.localPath(strFileCandidate);
            if(strFile == null || !strFile.exists()) {
                throw new IOException("Main Stratego file at " + strFile + " does not exist");
            }
        } else {
            throw new IOException("Main Stratego file does not exist");
        }

        final String strExternalJarFlags = config.strExternalJarFlags();

        final Iterable<FileObject> strIncludePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, SpoofaxConstants.LANG_STRATEGO_NAME);
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

        final ArrayList<STask<?>> sdfTasks = new ArrayList<>(0);

        // Gather all Stratego files to be checked for changes
        final Set<Path> changedFiles = GenerateSourcesBuilder.getChangedFiles(projectLocation);
        final Set<ResourceKey> changedResources = new HashSet<>(changedFiles.size() * 2);
        for(Path changedFile : changedFiles) {
            changedResources.add(new FSPath(changedFile));
        }

        final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>();
        GenerateSourcesBuilder.splitOffLinkedLibrariesIncludeDirs(extraArgs, linkedLibraries, strjIncludeDirs);
        final LastModified<IStrategoTerm> astWLM =
            new LastModified<>(ast, Instant.now().getEpochSecond());
        final ModuleIdentifier moduleIdentifier =
            new ModuleIdentifier(false, NameUtil.toJavaId(strModule.toLowerCase()), new FSPath(strFile));
        final CheckModuleInput checkModuleInput = new CheckModuleInput(new FrontInput.FileOpenInEditor(moduleIdentifier, sdfTasks,
            strjIncludeDirs, linkedLibraries, astWLM), moduleIdentifier);
        final Task<CheckModuleOutput> checkModuleTask = checkModuleProvider.get().createTask(checkModuleInput);

        final IPieProvider pieProvider = pieProviderProvider.get();
        final Pie pie = pieProvider.pie();

        final IStrategoList.Builder errors = B.listBuilder();
        final IStrategoList.Builder warnings = B.listBuilder();
        final IStrategoList.Builder notes = B.listBuilder();
        final CheckModuleOutput analysisInformation;
        synchronized(pie) {
            GenerateSourcesBuilder.initCompiler(pieProvider, checkModuleTask);
            try(final MixedSession session = pie.newSession()) {
                TopDownSession tdSession = session.updateAffectedBy(changedResources);
                session.deleteUnobservedTasks(t -> true, (t, r) -> {
                    if(r instanceof HierarchicalResource
                        && Objects.equals(((HierarchicalResource) r).getLeafExtension(), "java")) {
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
            final ImploderAttachment imploderAttachment =
                ImploderAttachment.get(OriginAttachment.tryGetOrigin(message.locationTerm));
            if(imploderAttachment == null) {
                logger.debug("No origins for message: " + message);
            }
            final IStrategoTuple messageTuple = B.tuple(message.locationTerm, B.string(message.getMessage()));
            if(config.strGradualSetting() == StrategoGradualSetting.NONE && message instanceof TypeMessage) {
                continue;
            }
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

        return B.tuple(B.list(errors), B.list(warnings), B.list(notes));
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
    }
}
