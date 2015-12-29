package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    private static Injector injector;
    private static IResourceService resourceService;
    private static ILanguagePathService languagePathService;
    private static IProjectService projectService;

    public final SpoofaxProjectSettings settings;
    public final File baseDir;
    public final File depDir;

    public transient FileObject base;
    public transient IProject project;


    public static void init(Injector newInjector) {
        if(injector != null) {
            return;
        }

        injector = newInjector;
        resourceService = newInjector.getInstance(IResourceService.class);
        languagePathService = newInjector.getInstance(ILanguagePathService.class);
        projectService = newInjector.getInstance(IProjectService.class);
    }


    public SpoofaxContext(SpoofaxProjectSettings settings) {
        if(injector == null) {
            throw new RuntimeException("Creating context while injector has not been set");
        }

        this.settings = settings;
        this.baseDir = toFile(settings.location());
        this.depDir = toFile(settings.getBuildDirectory());

        this.project = projectService.get(settings.location());
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


    public ILanguagePathService languagePathService() {
        return languagePathService;
    }


    public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
        final File strategoPath = toFile(settings.getStrMainFile());
        result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
        return buildStrategoEnabled;
    }

    public boolean isJavaJarEnabled(Builder<?, ?> result) {
        final File mainFile = toFile(settings.getStrJavaStrategiesMainFile());
        result.require(mainFile, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        return FileCommands.exists(mainFile);
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        settings.initAfterDeserialization(resourceService);
        this.base = resourceService.resolve(baseDir);
        this.project = projectService.get(settings.location());
    }
}
