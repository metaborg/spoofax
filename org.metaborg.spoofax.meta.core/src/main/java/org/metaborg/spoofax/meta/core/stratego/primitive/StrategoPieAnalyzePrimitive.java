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
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.build.main.GenerateSourcesBuilder;
import org.metaborg.spoofax.meta.core.pluto.build.main.IPieProvider;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.attachments.OriginAttachment;

import com.google.inject.Inject;
import com.google.inject.Provider;

import mb.flowspec.terms.B;
import mb.flowspec.terms.StrategoArrayList;
import mb.pie.api.ExecException;
import mb.pie.api.PieSession;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.stratego.build.strincr.Analysis;
import mb.stratego.build.strincr.Analysis.Output;
import mb.stratego.build.strincr.Message;
import mb.stratego.build.strincr.StrIncrAnalysis;

public class StrategoPieAnalyzePrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {
    private static final ILogger logger = LoggerUtils.logger(StrategoPieAnalyzePrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;
    @Inject private static Provider<IPieProvider> pieProviderProvider;

    private final StrIncrAnalysis strIncrAnalysis;
    private final ILanguagePathService languagePathService;
    private final IResourceService resourceService;

    @Inject public StrategoPieAnalyzePrimitive(StrIncrAnalysis strIncrAnalysis,
        ILanguagePathService languagePathService, IResourceService resourceService) {
        super("stratego_pie_analyze", 0, 0);
        this.strIncrAnalysis = strIncrAnalysis;
        this.languagePathService = languagePathService;
        this.resourceService = resourceService;
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) throws MetaborgException, IOException {
        final IStrategoAppl ast = Tools.applAt(current, 0);
        @SuppressWarnings("unused") final String path = Tools.javaStringAt(current, 1);
        @SuppressWarnings("unused") final String projectPath = Tools.javaStringAt(current, 2);

        if(!(ast.getName().equals("Module") && ast.getSubtermCount() == 2)) {
            throw new MetaborgException("Input AST for Stratego analysis not Module/2.");
        }
        final String moduleName = Tools.javaStringAt(ast, 0);

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
        final FileObject buildInfoLoc = paths.plutoBuildInfoDir();
        final SpoofaxContext spoofaxContext = new SpoofaxContext(baseLoc, buildInfoLoc);

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
        Analysis.Input strIncrAnalysisInput =
            new Analysis.Input(strFile, strjIncludeDirs, builtinLibs, originTasks, projectLocation);
        final Task<Output> strIncrAnalysisTask = strIncrAnalysis.createTask(strIncrAnalysisInput);

        try {
            GenerateSourcesBuilder.initCompiler(spoofaxContext.pieProvider(), strIncrAnalysisTask);
        } catch(ExecException e) {
            throw new MetaborgException("Initial Stratego build failed", e);
        }

        final ArrayList<IStrategoTerm> errors = new ArrayList<>();
        final ArrayList<IStrategoTerm> warnings = new ArrayList<>();
        final ArrayList<IStrategoTerm> notes = new ArrayList<>();
        try(final PieSession pieSession = pieProviderProvider.get().pie().newSession()) {
            Analysis.Output analysisInformation = pieSession.require(strIncrAnalysisTask);

            for(Message message : analysisInformation.staticCheckOutput.messages) {
                if(message.module.equals(moduleName)) {
                    logger.debug("Origins: " + message.name.getAttachment(OriginAttachment.TYPE));
                    errors.add(B.tuple(message.name, B.string(message.getMessage())));
                }
            }
        } catch(ExecException e) {
            throw new MetaborgException("Incremental Stratego build failed", e);
        }

        return B.tuple(StrategoArrayList.fromList(errors), StrategoArrayList.fromList(warnings),
            StrategoArrayList.fromList(notes));
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
