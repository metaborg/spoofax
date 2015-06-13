package org.metaborg.spoofax.meta.core;

import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.LANG_SDF;
import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.LANG_STRATEGO;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.PropertyHelper;
import org.metaborg.spoofax.core.build.dependency.IDependencyService;
import org.metaborg.spoofax.core.build.paths.ILanguagePathService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.generator.project.ProjectSettings;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class SpoofaxMetaBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxMetaBuilder.class);

    private final IResourceService resourceService;
    private final IDependencyService dependencyService;
    private final ILanguagePathService languagePathService;
    private final SpoofaxBuilder builder;

    @Inject public SpoofaxMetaBuilder(IResourceService resourceService, IDependencyService dependencyService,
        ILanguagePathService languagePathService, SpoofaxBuilder builder) {
        this.resourceService = resourceService;
        this.dependencyService = dependencyService;
        this.languagePathService = languagePathService;
        this.builder = builder;
    }

    public void generateSources(MetaBuildInput input) throws Exception {
        Iterable<ILanguage> compileLanguages = dependencyService.compileDependencies(input.project);
        Multimap<ILanguage, FileObject> sources = HashMultimap.create();
        Multimap<ILanguage, FileObject> includes = HashMultimap.create();
        Collection<ILanguage> pardonedLanguages = Lists.newArrayList();
        for(ILanguage language : compileLanguages) {
            sources.putAll(language, languagePathService.sources(input.project, language.name()));
            includes.putAll(language, languagePathService.includes(input.project, language.name()));
            if(input.pardonedLanguages.contains(language.name())) {
                pardonedLanguages.add(language);
            }
        }
        builder.build(new CompileGoal(), sources, includes, pardonedLanguages);
    }

    public void compilePreJava(MetaBuildInput input) {
        AntRunner runner = new AntRunner(input);
        runner.executeTarget("generate-sources");
    }

    public void compilePostJava(MetaBuildInput input) {
        AntRunner runner = new AntRunner(input);
        runner.executeTarget("package");
    }

    private class AntRunner {
        private final MetaBuildInput input;
        private final Project project;

        public AntRunner(MetaBuildInput input) {
            this.input = input;
            File antdir = getURLAsFile(getClass().getResource("ant"));
            File buildFile = new File(antdir, "build.main.xml");
            this.project = getProject(buildFile);
            setProperties();
            parseBuildFile(buildFile);
        }

        private Project getProject(File buildFile) throws BuildException {
            File basedir = resourceService.localPath(input.project.location());
            Project project = new Project();
            project.setBaseDir(basedir);
            project.setProperty("ant.file", buildFile.getPath());
            project.init();
            project.addBuildListener(new AntSLF4JLogger(log));
            return project;
        }

        private void setProperties() {
            PropertyHelper ph = PropertyHelper.getPropertyHelper(project);

            File distpath = getURLAsFile(NativeBundle.getDist());
            ph.setUserProperty("distpath", distpath.getPath());
            File nativepath = getURLAsFile(NativeBundle.getNative());
            restoreExecutablePermissions(nativepath);
            ph.setUserProperty("nativepath", nativepath.getPath());

            ph.setUserProperty("lang.name", input.projectSettings.name());
            ph.setUserProperty("lang.strname", input.projectSettings.strategoName());
            ph.setUserProperty("lang.format", input.projectSettings.format().name());
            ph.setUserProperty("lang.package.name", input.projectSettings.packageName());
            ph.setUserProperty("lang.package.path", input.projectSettings.packagePath());

            ph.setUserProperty("sdf.args", formatArgs(buildSdfArgs()));
            ph.setUserProperty("stratego.args", formatArgs(buildStrategoArgs()));

            if(input.externalDef != null) {
                ph.setUserProperty("externaldef", input.externalDef);
            }
            if(input.externalJar != null) {
                ph.setUserProperty("externaljar", input.externalJar);
            }
            if(input.externalJarFlags != null) {
                ph.setUserProperty("externaljarflags", input.externalJarFlags);
            }
        }

        private List<String> buildSdfArgs() {
            List<String> args = Lists.newArrayList(input.sdfArgs);
            Iterable<FileObject> paths = languagePathService.sourcesAndIncludes(input.project, LANG_SDF);
            for(FileObject path : paths) {
                File file = resourceService.localFile(path);
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

        private List<String> buildStrategoArgs() {
            List<String> args = Lists.newArrayList(input.strategoArgs);
            Iterable<FileObject> paths = languagePathService.sourcesAndIncludes(input.project, LANG_STRATEGO);
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
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", helper);
            helper.parse(project, buildFile);
        }

        public void executeTarget(String name) {
            project.executeTarget(name);
        }

        private String formatArgs(List<String> args) {
            String ret = "";
            for(String arg : args) {
                ret += " " + formatArg(arg);
            }
            return ret;
        }

        private String formatArg(String arg) {
            return StringUtils.containsWhitespace(arg) ? "\"" + arg + "\"" : arg;
        }

        private void restoreExecutablePermissions(File directory) {
            for(File fileOrDirectory : directory.listFiles()) {
                if(fileOrDirectory.isDirectory()) {
                    restoreExecutablePermissions(fileOrDirectory);
                } else {
                    fileOrDirectory.setExecutable(true);
                }
            }
        }

    }

    private File getURLAsFile(URL url) {
        FileObject resource = resourceService.resolve(url.toString());
        File localFile = resourceService.localFile(resource);
        return localFile;
    }

    public static class MetaBuildInput {
        public final IProject project;
        public final Collection<String> pardonedLanguages;
        public final ProjectSettings projectSettings;
        public final List<String> sdfArgs;
        public final List<String> strategoArgs;
        public File externalDef;
        public String externalJar;
        public String externalJarFlags;

        public MetaBuildInput(IProject project, Collection<String> pardonedLanguages, ProjectSettings projectSettings,
            List<String> sdfArgs, List<String> strategoArgs) {
            this.project = project;
            this.pardonedLanguages = pardonedLanguages;
            this.projectSettings = projectSettings;
            this.sdfArgs = sdfArgs;
            this.strategoArgs = strategoArgs;
        }

    }

}
