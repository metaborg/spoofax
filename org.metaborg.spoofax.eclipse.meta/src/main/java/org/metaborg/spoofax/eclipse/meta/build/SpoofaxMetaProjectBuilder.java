package org.metaborg.spoofax.eclipse.meta.build;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.meta.language.LoadLanguageJob;
import org.metaborg.spoofax.eclipse.meta.legacy.build.LegacyBuildProperties;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.BundleUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class SpoofaxMetaProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxMetaPlugin.id + ".builder";

    private static final Logger logger = LoggerFactory.getLogger(SpoofaxMetaProjectBuilder.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;

    private final GlobalSchedulingRules globalSchedulingRules;


    public SpoofaxMetaProjectBuilder() {
        final Injector injector = SpoofaxMetaPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageDiscoveryService = injector.getInstance(ILanguageDiscoveryService.class);
        this.globalSchedulingRules = injector.getInstance(GlobalSchedulingRules.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        try {
            if(kind != AUTO_BUILD) {
                build(getProject(), monitor);
            }
        } catch(IOException e) {
            logger.error("Cannot build language project", e);
        } finally {
            // Always forget last build state to force a full build next time.
            forgetLastBuiltState();
        }
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        try {
            clean(getProject(), monitor);
        } catch(IOException e) {
            logger.error("Cannot clean language project", e);
        } finally {
            // Always forget last build state to force a full build next time.
            forgetLastBuiltState();
        }
    }

    private void runAnt(final IProject project, String buildFilePath, String[] targets, IProgressMonitor monitor)
        throws CoreException, IOException {
        final AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFilePath);
        runner.setExecutionTargets(targets);
        final URL[] classpaths = classpaths();
        runner.setCustomClasspath(classpaths);
        final Map<String, String> properties = properties(classpaths);
        runner.addUserProperties(properties);
        runner.addBuildLogger("org.metaborg.spoofax.eclipse.meta.build.SpoofaxAntBuildLogger");
        final IWorkspaceRunnable antRunnable = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                runner.run(workspaceMonitor);
                project.refreshLocal(IResource.DEPTH_INFINITE, workspaceMonitor);
            }
        };
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(antRunnable, project, IWorkspace.AVOID_UPDATE, monitor);
    }

    private void clean(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
        logger.debug("Cleaning language project {}", project);
        final String buildFilePath = buildFilePath(project, "clean");
        runAnt(project, buildFilePath, new String[] { "clean" }, monitor);
    }

    private void build(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
        logger.debug("Building language project {}", project);
        final String buildFilePath = buildFilePath(project, "build");
        runAnt(project, buildFilePath, new String[] { "all" }, monitor);

        final FileObject projectResource = resourceService.resolve(project);
        final Job languageLoadJob = new LoadLanguageJob(languageDiscoveryService, projectResource);
        languageLoadJob.setRule(new MultiRule(new ISchedulingRule[] { globalSchedulingRules.startupReadLock(),
            globalSchedulingRules.languageServiceLock() }));
        languageLoadJob.schedule();
    }

    private String buildFilePath(IProject project, String action) throws FileSystemException, CoreException {
        final FileObject projectLocation = resourceService.resolve(project);
        final FileObject buildFileLocation = projectLocation.resolveFile("build.main.xml");
        if(!buildFileLocation.exists()) {
            final String message =
                String.format("Cannot %s language project, build file %s does not exist", action, buildFileLocation);
            logger.error(message);
            throw new CoreException(StatusUtils.error(message));
        }
        final File buildFile = resourceService.localFile(buildFileLocation);
        if(buildFile == null) {
            final String message =
                String.format("Cannot %s language project, build file %s does not reside on the local file system",
                    action, buildFileLocation);
            logger.error(message);
            throw new CoreException(StatusUtils.error(message));
        }
        final String buildFilePath = buildFile.getAbsolutePath();
        return buildFilePath;
    }

    private Map<String, String> properties(URL[] classpaths) throws IOException {
        final Map<String, String> properties = LegacyBuildProperties.properties();

        final Collection<String> pluginClasspathPaths = Lists.newLinkedList();
        for(URL classpath : classpaths) {
            pluginClasspathPaths.add(classpath.getPath());
        }
        properties.put("externaljarx", Joiner.on(File.pathSeparator).join(pluginClasspathPaths));

        return properties;
    }

    /**
     * @return List of classpath entries generated from installed Eclipse plugins.
     */
    private URL[] classpaths() throws MalformedURLException {
        final Collection<URL> classpath = LegacyBuildProperties.classpaths();
        final Map<String, Bundle> bundles = BundleUtils.bundlesBySymbolicName(SpoofaxMetaPlugin.context());

        final Bundle antBundle = bundles.get("org.apache.ant");
        if(antBundle == null) {
            logger.error("Could not find Ant bundle 'org.apache.ant', language build will probably fail");
        } else {
            try {
                final File file = FileLocator.getBundleFile(antBundle);
                final String path = file.getAbsolutePath();
                final File lib = Paths.get(path, "lib").toFile();
                final File[] jarFiles = lib.listFiles(new FilenameFilter() {
                    @Override public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });
                for(File jarFile : jarFiles) {
                    classpath.add(jarFile.toURI().toURL());
                }
            } catch(IOException e) {
                logger.error("Error while adding 'org.apache.ant' to classpath for Ant build, "
                    + "language build will probably fail", e);
            }
        }

        for(final Bundle bundle : bundles.values()) {
            try {
                final File file = FileLocator.getBundleFile(bundle);
                final String path = file.getAbsolutePath();
                if(path.endsWith(".jar")) {
                    // An installed JAR plugin.
                    classpath.add(file.toURI().toURL());
                    continue;
                }

                final File targetClasses = Paths.get(path, "target", "classes").toFile();
                final File bin = Paths.get(path, "bin").toFile();
                if(targetClasses.exists()) {
                    // A plugin under development with all its classes in the target/classes directory.
                    classpath.add(targetClasses.toURI().toURL());
                } else if(bin.exists()) {
                    // A plugin under development with all its classes in the bin directory.
                    classpath.add(bin.toURI().toURL());
                } else {
                    // An installed unpacked plugin. Class files are extracted in this directory.
                    classpath.add(file.toURI().toURL());
                }

                // Also include any nested jar files.
                final File[] jarFiles = file.listFiles(new FilenameFilter() {
                    @Override public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });
                for(File jarFile : jarFiles) {
                    classpath.add(jarFile.toURI().toURL());
                }
            } catch(IOException e) {
                logger.error("Error while creating classpath for Ant build", e);
            }
        }

        return classpath.toArray(new URL[0]);
    }
}
