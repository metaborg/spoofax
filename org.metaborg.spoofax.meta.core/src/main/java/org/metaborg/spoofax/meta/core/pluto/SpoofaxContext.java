package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.ITermFactory;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    // NOTE: This class should only contain static or transient fields.
    // If non-transient fields are required, they must be Serializable,
    // (so FileObject is out of the question).

    private static Injector injector;
    private static IResourceService resourceService;
    private static ILanguageService languageService;
    private static ILanguageIdentifierService languageIdentifierService;
    private static ILanguagePathService languagePathService;
    private static IProjectService projectService;
    private static ILanguageSpecService languageSpecService;
    private static ISourceTextService sourceTextService;
    private static ISpoofaxSyntaxService syntaxService;
    private static ITermFactoryService termFactoryService;

    public final File baseDir;
    public final URI baseURI;
    public final File depDir;

    public transient FileObject base;
    private transient IProject project;
    public transient ILanguageSpec languageSpec;


    public static void init(Injector newInjector) {
        if (injector != null) {
            return;
        }

        injector = newInjector;
        resourceService = newInjector.getInstance(IResourceService.class);
        languageService = newInjector.getInstance(ILanguageService.class);
        languageIdentifierService = newInjector.getInstance(ILanguageIdentifierService.class);
        languagePathService = newInjector.getInstance(ILanguagePathService.class);
        projectService = newInjector.getInstance(IProjectService.class);
        languageSpecService = newInjector.getInstance(ILanguageSpecService.class);
        sourceTextService = newInjector.getInstance(ISourceTextService.class);
        syntaxService = newInjector.getInstance(ISpoofaxSyntaxService.class);
        termFactoryService = newInjector.getInstance(ITermFactoryService.class);
    }


    public SpoofaxContext(FileObject baseDir, FileObject depDir) {
        if (injector == null) {
            throw new RuntimeException("Creating context while injector has not been set");
        }

        
        this.baseDir = toFile(baseDir);
        this.baseURI = FileUtils.toURI(baseDir);
        this.depDir = toFile(depDir);

        init();
    }

    public void init() {
        this.base = this.resourceService().resolve(this.baseURI);
        this.project = projectService.get(this.base);
        this.languageSpec = languageSpecService.get(this.project);
    }


    public IResourceService resourceService() {
        return resourceService;
    }

    public File toFile(FileObject fileObject) {
        return resourceService.localPath(fileObject);
    }

    public File toFileReplicate(FileObject fileObject) {
        return resourceService.localFile(fileObject);
    }

    public File basePath(String relative) {
        return new File(baseDir, relative);
    }

    public File depPath(String relative) {
        return new File(depDir, relative);
    }

    public ResourceAgentTracker newResourceTracker(String... excludePatterns) {
        final ResourceAgentTracker tracker = new ResourceAgentTracker(resourceService, base, excludePatterns);
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(base);
        agent.setAbsoluteDefinitionDir(base);
        return tracker;
    }

    public ILanguageService languageService() {
        return languageService;
    }

    public ILanguageIdentifierService languageIdentifierService() {
        return languageIdentifierService;
    }

    public ILanguagePathService languagePathService() {
        return languagePathService;
    }

    public ISourceTextService sourceTextService() {
        return sourceTextService;
    }

    public ISpoofaxSyntaxService syntaxService() {
        return syntaxService;
    }

    public ITermFactoryService termFactoryService() {
        return termFactoryService;
    }

    public ITermFactory termFactory() {
        return termFactoryService.getGeneric();
    }


    public boolean isBuildStrategoEnabled(Builder<?, ?> result, File strategoMainFile) {
        final File strategoPath = strategoMainFile;
        result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
                : LastModifiedStamper.instance);
        return FileCommands.exists(strategoPath);
    }

    public boolean isJavaJarEnabled(Builder<?, ?> result, File strategoJavaStrategiesMainFile) {
        final File mainFile = strategoJavaStrategiesMainFile;
        result.require(mainFile, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
                : LastModifiedStamper.instance);
        return FileCommands.exists(mainFile);
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        init();
    }
}
