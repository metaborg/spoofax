package org.metaborg.spoofax.meta.core;

import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.LANG_SDF;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.LANG_STRATEGO;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.PropertyHelper;
import org.metaborg.spoofax.core.build.paths.ILanguagePathService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.nativebundle.NativeBundle;

import com.google.common.collect.Lists;

class AntRunner {
    private final IResourceService resourceService;
    private final ILanguagePathService languagePathService;

    private final MetaBuildInput input;
    private final Project antProject;


    public AntRunner(IResourceService resourceService, ILanguagePathService languagePathService, MetaBuildInput input,
        @Nullable ClassLoader classLoader) {
        this.resourceService = resourceService;
        this.languagePathService = languagePathService;

        this.input = input;

        Thread.currentThread().setContextClassLoader(classLoader);

        final File antDir = getURLAsFile(getClass().getResource("ant"), resourceService);
        final File buildFile = new File(antDir, "build.main.xml");

        this.antProject = new Project();
        final File basedir = resourceService.localPath(input.project.location());
        antProject.setBaseDir(basedir);
        antProject.setProperty("ant.file", buildFile.getPath());
        //antProject.setCoreLoader(classLoader);
        antProject.init();
        antProject.addBuildListener(new AntSLF4JLogger(SpoofaxMetaBuilder.log));

        setProperties();
        parseBuildFile(buildFile);
    }


    public void executeTarget(String target) {
        antProject.executeTarget(target);
    }


    private void setProperties() {
        final PropertyHelper helper = PropertyHelper.getPropertyHelper(antProject);

        final File distpath = getURLAsFile(NativeBundle.getDist(), resourceService);
        helper.setUserProperty("distpath", distpath.getPath());
        final File nativepath = getURLAsFile(NativeBundle.getNative(), resourceService);
        restoreExecutablePermissions(nativepath);
        helper.setUserProperty("nativepath", nativepath.getPath());

        helper.setUserProperty("lang.name", input.projectSettings.name());
        helper.setUserProperty("lang.strname", input.projectSettings.strategoName());
        helper.setUserProperty("lang.format", input.projectSettings.format().name());
        helper.setUserProperty("lang.package.name", input.projectSettings.packageName());
        helper.setUserProperty("lang.package.path", input.projectSettings.packagePath());

        helper.setUserProperty("sdf.args", formatArgs(buildSdfArgs()));
        helper.setUserProperty("stratego.args", formatArgs(buildStrategoArgs()));

        if(input.externalDef != null) {
            helper.setUserProperty("externaldef", input.externalDef);
        }
        if(input.externalJar != null) {
            helper.setUserProperty("externaljar", input.externalJar);
        }
        if(input.externalJarFlags != null) {
            helper.setUserProperty("externaljarflags", input.externalJarFlags);
        }
    }

    private Collection<String> buildSdfArgs() {
        final Collection<String> args = Lists.newArrayList(input.sdfArgs);
        final Iterable<FileObject> paths = languagePathService.sourcesAndIncludes(input.project, LANG_SDF);
        for(FileObject path : paths) {
            final File file = resourceService.localFile(path);
            if(file.exists()) {
                if(path.getName().getExtension().equals("def")) {
                    args.add("-Idef");
                    args.add(file.getPath());
                } else {
                    args.add("-I");
                    args.add(file.getPath());
                }
            }
        }
        return args;
    }

    private Collection<String> buildStrategoArgs() {
        final Collection<String> args = Lists.newArrayList(input.strategoArgs);
        final Iterable<FileObject> paths = languagePathService.sourcesAndIncludes(input.project, LANG_STRATEGO);
        for(FileObject path : paths) {
            File file = resourceService.localFile(path);
            if(file.exists()) {
                args.add("-I");
                args.add(file.getPath());
            }
        }
        return args;
    }

    private void parseBuildFile(File buildFile) throws BuildException {
        final ProjectHelper helper = ProjectHelper.getProjectHelper();
        antProject.addReference("ant.projectHelper", helper);
        helper.parse(antProject, buildFile);
    }


    private static File getURLAsFile(URL url, IResourceService resourceService) {
        final FileObject resource = resourceService.resolve(url.toString());
        final File localFile = resourceService.localFile(resource);
        return localFile;
    }

    private static String formatArgs(Collection<String> args) {
        String ret = "";
        for(String arg : args) {
            ret += " " + formatArg(arg);
        }
        return ret;
    }

    private static String formatArg(String arg) {
        return StringUtils.containsWhitespace(arg) ? "\"" + arg + "\"" : arg;
    }

    private static void restoreExecutablePermissions(File directory) {
        for(File fileOrDirectory : directory.listFiles()) {
            if(fileOrDirectory.isDirectory()) {
                restoreExecutablePermissions(fileOrDirectory);
            } else {
                fileOrDirectory.setExecutable(true);
            }
        }
    }
}