package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    public static Injector injector;
    public static IResourceService resourceService;
    public static ILanguagePathService languagePathService;
    public static IProjectService projectService;
    
    public final SpoofaxProjectSettings settings;
    public final File baseDir;
    public final File depDir;
    public final Iterable<String> javaClasspath;
    
    public transient IProject project;
    

    public static void init(Injector injector) {
        if(SpoofaxContext.injector != null) {
            throw new RuntimeException("Setting injector while it has already been set");
        }
        
        SpoofaxContext.injector = injector;
        resourceService = injector.getInstance(IResourceService.class);
        languagePathService = injector.getInstance(ILanguagePathService.class);
        projectService = injector.getInstance(IProjectService.class);
    }
    
    
    public SpoofaxContext(SpoofaxProjectSettings settings, Iterable<String> javaClasspath) {
        if(SpoofaxContext.injector == null) {
            throw new RuntimeException("Creating context while injector has not been set");
        }
        
        this.settings = settings;
        this.baseDir = FileUtils.toFile(settings.location());
        this.depDir = FileUtils.toFile(settings.getBuildDirectory());
        this.javaClasspath = javaClasspath;
        
        this.project = projectService.get(settings.location());
    }


    public File basePath(String relative) {
        return new File(baseDir, relative);
    }
    
    public File depPath(String relative) {
        return new File(depDir, relative);
    }
    

    public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
        final File strategoPath = FileUtils.toFile(settings.getStrMainFile());
        result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
        return buildStrategoEnabled;
    }

    public boolean isJavaJarEnabled(Builder<?, ?> result) {
        final File mainFile = FileUtils.toFile(settings.getStrJavaStrategiesMainFile());
        result.require(mainFile, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        return FileCommands.exists(mainFile);
    }
    
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        settings.initAfterDeserialization(resourceService);
        this.project = projectService.get(settings.location());
    }
}
