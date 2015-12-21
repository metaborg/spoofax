package org.metaborg.spoofax.meta.core;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.tools.ant.BuildListener;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.ant.IAntRunner;
import org.metaborg.spoofax.meta.core.ant.IAntRunnerService;
import org.metaborg.spoofax.nativebundle.NativeBundle;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_SDF_NAME;
import static org.metaborg.spoofax.core.SpoofaxConstants.LANG_STRATEGO_NAME;

class NewMetaBuildAntRunnerFactory {
    private final IResourceService resourceService;
    private final ILanguagePathService languagePathService;
    private final IAntRunnerService antRunnerService;


    @Inject public NewMetaBuildAntRunnerFactory(IResourceService resourceService,
                                                ILanguagePathService languagePathService, IAntRunnerService antRunnerService) {
        this.resourceService = resourceService;
        this.languagePathService = languagePathService;
        this.antRunnerService = antRunnerService;
    }


    public IAntRunner create(LanguageSpecBuildInput input, @Nullable URL[] classpaths, @Nullable BuildListener listener)
        throws FileSystemException {
        final ISpoofaxLanguageSpecConfig config = input.config;
        final FileObject antFile = resourceService.resolve(getClass().getResource("build.xml").toString());
        final FileObject baseDir = input.languageSpec.location();

        final Map<String, String> properties = Maps.newHashMap();

        final FileObject mixPath = resourceService.resolve(NativeBundle.getStrategoMix());
        final FileObject distPath = mixPath.getParent();
        final File localDistPath = resourceService.localFile(distPath);
        properties.put("distpath", localDistPath.getPath());
        final FileObject nativePath = resourceService.resolve(NativeBundle.getNativeDirectory());
        final File localNativePath = resourceService.localFile(nativePath);
        restoreExecutablePermissions(localNativePath);
        properties.put("nativepath", localNativePath.getPath());

        properties.put("lang.name", config.name());
        properties.put("lang.strname", config.strategoName());
        properties.put("lang.format", config.format().name());
        properties.put("lang.package.name", config.packageName());
        properties.put("lang.package.path", config.packagePath());

        properties.put("sdf.args", formatArgs(buildSdfArgs(input)));
        if(config.externalDef() != null) {
            properties.put("externaldef", config.externalDef());
        }

        properties.put("stratego.args", formatArgs(buildStrategoArgs(input)));
        if(config.externalJar() != null) {
            properties.put("externaljar", config.externalJar());
        }
        if(config.externalJarFlags() != null) {
            properties.put("externaljarflags", config.externalJarFlags());
        }

        return antRunnerService.get(antFile, baseDir, properties, classpaths, listener);
    }

    private Collection<String> buildSdfArgs(LanguageSpecBuildInput input) {
        final ISpoofaxLanguageSpecConfig config = input.config;
        final Collection<String> args = Lists.newArrayList(config.sdfArgs());
        final Iterable<FileObject> paths = languagePathService.sourceAndIncludePaths(input.languageSpec, LANG_SDF_NAME);
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

    private Collection<String> buildStrategoArgs(LanguageSpecBuildInput input) {
        final ISpoofaxLanguageSpecConfig config = input.config;
        final Collection<String> args = Lists.newArrayList(config.strategoArgs());
        final Iterable<FileObject> paths = languagePathService.sourceAndIncludePaths(input.languageSpec, LANG_STRATEGO_NAME);
        // BOOTSTRAPPING: Stratego language name was wrongly named "Stratego" instead of "Stratego-Sugar". Also include
        // paths from the wrong language name to support the older baseline languages used for bootstrapping.
        final Iterable<FileObject> legacyPaths = languagePathService.sourceAndIncludePaths(input.languageSpec, "Stratego");
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
