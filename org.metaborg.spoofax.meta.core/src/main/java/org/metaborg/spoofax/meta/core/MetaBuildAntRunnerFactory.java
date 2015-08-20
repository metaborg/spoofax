package org.metaborg.spoofax.meta.core;

import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_SDF_NAME;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_STRATEGO_NAME;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;
import org.metaborg.spoofax.nativebundle.NativeBundle;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

class MetaBuildAntRunnerFactory {
    private final IResourceService resourceService;
    private final ILanguagePathService languagePathService;
    private final IAntRunnerService antRunnerService;


    @Inject public MetaBuildAntRunnerFactory(IResourceService resourceService,
        ILanguagePathService languagePathService, IAntRunnerService antRunnerService) {
        this.resourceService = resourceService;
        this.languagePathService = languagePathService;
        this.antRunnerService = antRunnerService;
    }


    public IAntRunner create(MetaBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener)
        throws FileSystemException {
        final SpoofaxProjectSettings projectSettings = input.settings;
        final FileObject antFile = resourceService.resolve(getClass().getResource("build.xml").toString());
        final FileObject baseDir = input.project.location();

        final Map<String, String> properties = Maps.newHashMap();

        final FileObject distPath = resourceService.resolve(NativeBundle.getDist().toString());
        final File localDistPath = resourceService.localFile(distPath);
        properties.put("distpath", localDistPath.getPath());
        final FileObject nativePath = resourceService.resolve(NativeBundle.getNative().toString());
        final File localNativePath = resourceService.localFile(nativePath);
        restoreExecutablePermissions(localNativePath);
        properties.put("nativepath", localNativePath.getPath());

        properties.put("lang.name", input.settings.settings().name());
        properties.put("lang.strname", input.settings.strategoName());
        properties.put("lang.format", input.settings.format().name());
        properties.put("lang.package.name", input.settings.packageName());
        properties.put("lang.package.path", input.settings.packagePath());

        properties.put("sdf.args", formatArgs(buildSdfArgs(input)));
        if(projectSettings.externalDef() != null) {
            properties.put("externaldef", projectSettings.externalDef());
        }

        properties.put("stratego.args", formatArgs(buildStrategoArgs(input)));
        if(projectSettings.externalJar() != null) {
            properties.put("externaljar", projectSettings.externalJar());
        }
        if(projectSettings.externalJarFlags() != null) {
            properties.put("externaljarflags", projectSettings.externalJarFlags());
        }

        return antRunnerService.get(antFile, baseDir, properties, classpaths, listener);
    }

    private Collection<String> buildSdfArgs(MetaBuildInput input) {
        final SpoofaxProjectSettings projectSettings = input.settings;
        final Collection<String> args = Lists.newArrayList(projectSettings.sdfArgs());
        final Iterable<FileObject> paths = languagePathService.sourceAndIncludePaths(input.project, LANG_SDF_NAME);
        for(FileObject path : paths) {
            try {
                if(path.exists()) {
                    final File file = resourceService.localFile(path);
                    if(path.getName().getExtension().equals("def")) {
                        args.add("-Idef");
                        args.add(file.getPath());
                    } else {
                        args.add("-I");
                        args.add(file.getPath());
                    }
                }
            } catch(FileSystemException e) {
                // Ignore path if path.exists fails.
            }
        }
        return args;
    }

    private Collection<String> buildStrategoArgs(MetaBuildInput input) {
        final SpoofaxProjectSettings projectSettings = input.settings;
        final Collection<String> args = Lists.newArrayList(projectSettings.strategoArgs());
        final Iterable<FileObject> paths = languagePathService.sourceAndIncludePaths(input.project, LANG_STRATEGO_NAME);
        // BOOTSTRAPPING: Stratego language name was wrongly named "Stratego" instead of "Stratego-Sugar". Also include
        // paths from the wrong language name to support the older baseline languages used for bootstrapping.
        final Iterable<FileObject> legacyPaths = languagePathService.sourceAndIncludePaths(input.project, "Stratego");
        for(FileObject path : Iterables.concat(paths, legacyPaths)) {
            File file = resourceService.localFile(path);
            if(file.exists()) {
                args.add("-I");
                args.add(file.getPath());
            }
        }
        return args;
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
