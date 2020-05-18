package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.StrategoGradualSetting;
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
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.stratego.build.strincr.Frontends;
import mb.stratego.build.strincr.Frontends.Output;
import mb.stratego.build.strincr.Message;
import mb.stratego.build.strincr.StrIncrAnalysis;

public class StrategoPieAnalyzePrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoPieAnalyzePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;
    @Inject private static Provider<IPieProvider> pieProviderProvider;

    // Using provider to break cycle between StrIncrAnalysis -> Stratego runtime -> all primitives -> this primitive
    private final Provider<StrIncrAnalysis> strIncrAnalysisProvider;
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;

    @Inject public StrategoPieAnalyzePrimitive(Provider<StrIncrAnalysis> strIncrAnalysisProvider,
        ILanguagePathService languagePathService, IResourceService resourceService) {
        super("stratego_pie_analyze", 0, 0);
        this.strIncrAnalysisProvider = strIncrAnalysisProvider;
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        @SuppressWarnings("unused") final IStrategoAppl ast = TermUtils.toApplAt(current, 0);
        final String path = TermUtils.toJavaStringAt(current, 1);
        @SuppressWarnings("unused") final String projectPath = TermUtils.toJavaStringAt(current, 2);

//        if(!(ast.getName().equals("Module") && ast.getSubtermCount() == 2)) {
//            throw new MetaborgException("Input AST for Stratego analysis not Module/2.");
//        }
//        final String moduleName = TermUtils.toJavaStringAt(ast, 0);

        final IProject project = context.project();
        if(project == null) {
            logger.debug("Cannot find project for opened file, cancelling analysis. ");
            return null;
        }

        if(languageSpecServiceProvider == null || pieProviderProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Spoofax meta services is not available; static injection failed");
            return null;
        }

        final ISpoofaxLanguageSpec languageSpec = getLanguageSpecification(project);
        final ISpoofaxLanguageSpecConfig config = languageSpec.config();
        final FileObject baseLoc = languageSpec.location();
        final SpoofaxLangSpecCommonPaths paths = new SpoofaxLangSpecCommonPaths(baseLoc);

        final String strModule = config.strategoName();

        final Iterable<FileObject> strRoots =
            languagePathService.sourcePaths(project, SpoofaxConstants.LANG_STRATEGO_NAME);
        final File strFile;
        final FileObject strFileCandidate = paths.findStrMainFile(strRoots, strModule);
        if(strFileCandidate != null && strFileCandidate.exists()) {
            strFile = resourceService.localPath(strFileCandidate);
            if(!strFile.exists()) {
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
        final List<File> strjIncludeDirs = new ArrayList<>();
        final List<File> strjIncludeFiles = new ArrayList<>();
        for(FileObject strIncludePath : strIncludePaths) {
            if(!strIncludePath.exists()) {
                continue;
            }
            if(strIncludePath.isFolder()) {
                strjIncludeDirs.add(resourceService.localFile(strIncludePath, strjIncludesReplicateDir));
            }
            if(strIncludePath.isFile()) {
                strjIncludeFiles.add(resourceService.localFile(strIncludePath, strjIncludesReplicateDir));
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

        final List<STask> sdfTasks = Collections.emptyList();

        // Gather all Stratego files to be checked for changes
        final Set<Path> changedFiles = GenerateSourcesBuilder.getChangedFiles(projectLocation);
        final Set<ResourceKey> changedResources = new HashSet<>(changedFiles.size() * 2);
        for(Path changedFile : changedFiles) {
            changedResources.add(new FSPath(changedFile));
        }

        final Arguments newArgs = new Arguments();
        final List<String> builtinLibs = GenerateSourcesBuilder.splitOffBuiltinLibs(extraArgs, newArgs);
        Collection<STask> originTasks = sdfTasks;
        Frontends.Input strIncrAnalysisInput =
            new Frontends.Input(strFile, strjIncludeDirs, builtinLibs, originTasks, projectLocation, config.strGradualSetting() == StrategoGradualSetting.on);
        final Task<Output> strIncrAnalysisTask = strIncrAnalysisProvider.get().createTask(strIncrAnalysisInput);

        final Pie pie = GenerateSourcesBuilder.initCompiler(pieProviderProvider.get(), strIncrAnalysisTask);

        final IStrategoList.Builder errors = B.listBuilder();
        final IStrategoList.Builder warnings = B.listBuilder();
        final IStrategoList.Builder notes = B.listBuilder();
        try(final PieSession pieSession = pie.newSession()) {
            Frontends.Output analysisInformation = pieSession.require(strIncrAnalysisTask);

            for(Message<?> message : analysisInformation.messages) {
                if(message.moduleFilePath.equals(path)) {
                    final ImploderAttachment imploderAttachment = ImploderAttachment.get(OriginAttachment.tryGetOrigin(message.locationTerm));
                    if(imploderAttachment == null) {
                        logger.debug("No origins for message: " + message);
                    }
                    final IStrategoTuple messageTuple = B.tuple(message.locationTerm, B.string(message.getMessage()));
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
            }
        } catch(ExecException e) {
            throw new MetaborgException("Incremental Stratego build failed", e);
        }

        return B.tuple(B.list(errors), B.list(warnings), B.list(notes));
    }

    private ISpoofaxLanguageSpec getLanguageSpecification(IProject project) throws MetaborgException {
        if(languageSpecServiceProvider == null) {
            // Indicates that meta-Spoofax is not available (ISpoofaxLanguageSpecService cannot be injected), but this
            // should never happen because this primitive is inside meta-Spoofax. Check for null just in case.
            logger.error("Language specification service is not available; static injection failed");
            return null;
        }
        final ISpoofaxLanguageSpecService languageSpecService = languageSpecServiceProvider.get();
        if(!languageSpecService.available(project)) {
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
