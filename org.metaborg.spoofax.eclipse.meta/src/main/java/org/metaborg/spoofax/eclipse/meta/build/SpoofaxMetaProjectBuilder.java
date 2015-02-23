package org.metaborg.spoofax.eclipse.meta.build;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class SpoofaxMetaProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxMetaPlugin.id + ".builder";

    private static final Logger logger = LoggerFactory.getLogger(SpoofaxMetaProjectBuilder.class);

    private final IEclipseResourceService resourceService;


    public SpoofaxMetaProjectBuilder() {
        final Injector injector = SpoofaxMetaPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        try {
            if(kind != AUTO_BUILD) {
                build(getProject(), monitor);
            }
        } catch(FileSystemException e) {
            logger.error("Cannot build language project", e);
        } finally {
            forgetLastBuiltState();
        }
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        try {
            clean(getProject(), monitor);
        } catch(FileSystemException e) {
            logger.error("Cannot clean language project", e);
        } finally {
            forgetLastBuiltState();
        }
    }

    private void clean(final IProject project, IProgressMonitor monitor) throws CoreException, FileSystemException {
        logger.debug("Cleaning language project {}", project);
        final String buildFilePath = buildFilePath(project, "clean");
        final AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFilePath);
        runner.setExecutionTargets(new String[] { "clean" });
        runner.setArguments(arguments());
        runner.setCustomClasspath(classpath());
        runner.addBuildLogger("org.metaborg.spoofax.eclipse.meta.build.SpoofaxAntBuildLogger");
        runner.run(monitor);
    }

    private void build(IProject project, IProgressMonitor monitor) throws CoreException, FileSystemException {
        logger.debug("Building language project {}", project);
        final String buildFilePath = buildFilePath(project, "build");
        final AntRunner runner = new AntRunner();
        runner.setBuildFileLocation(buildFilePath);
        runner.setExecutionTargets(new String[] { "all" });
        runner.setArguments(arguments());
        runner.setCustomClasspath(classpath());
        runner.addBuildLogger("org.metaborg.spoofax.eclipse.meta.build.SpoofaxAntBuildLogger");
        runner.run(monitor);
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

    private String[] arguments() {
        final Collection<URL> args = Lists.newLinkedList();
        // TODO: eclipse.spoofaximp.nativeprefix
        // TODO: eclipse.spoofaximp.strategojar
        // TODO: eclipse.spoofaximp.strategominjar
        // TODO: eclipse.spoofaximp.jars
        // TODO: externaljar
        // TODO: externaljarx
        // TODO: java.jar.classpath
        return args.toArray(new String[0]);
    }

    /**
     * @return List of classpath entries generated from installed Eclipse plugins.
     */
    private URL[] classpath() {
        final Collection<URL> classpath = Lists.newLinkedList();
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final Bundle[] bundles = bundleContext.getBundles();
        for(final Bundle bundle : bundles) {
            try {
                if(bundle.getSymbolicName().equals("org.apache.ant")) {
                    // Include Ant JAR files.
                    final File file = FileLocator.getBundleFile(bundle);
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
                    continue;
                }

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
                logger.error("Error while creating Ant classpath", e);
            }
        }

        return classpath.toArray(new URL[0]);
    }
}
