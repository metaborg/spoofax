package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaJar;
import build.pluto.output.None;

public class PackageBuilder extends SpoofaxBuilder<SpoofaxInput, None> {
    public static SpoofaxBuilderFactory<SpoofaxInput, None, PackageBuilder> factory = SpoofaxBuilderFactoryFactory.of(
        PackageBuilder.class, SpoofaxInput.class);


    public PackageBuilder(SpoofaxInput input) {
        super(input);
    }


    @Override protected String description(SpoofaxInput input) {
        return "Package";
    }

    @Override public File persistentPath(SpoofaxInput input) {
        return context.depPath("package.dep");
    }

    @Override protected None build(SpoofaxInput input) throws Throwable {
        // TODO: build Java code with Pluto.
        // BuildRequest<CompileJavaCode.Input, None, CompileJavaCode, ?> compileJavaCode =
        // new BuildRequest<>(CompileJavaCode.factory, new CompileJavaCode.Input(context,
        // new BuildRequest<?, ?, ?, ?>[] { strategoCtree }));
        // requireBuild(compileJavaCode);

        if(context.isJavaJarEnabled(this)) {
            final File buildDir = FileUtils.toFile(context.settings.getStrJavaDirectory());
            // TODO: get javajar-includes from project settings?
            // String[] paths = context.props.getOrElse("javajar-includes",
            // context.settings.packageStrategiesPath()).split("[\\s]+");
            final String[] paths = new String[] { context.settings.packageStrategiesPath() };
            final File output = FileUtils.toFile(context.settings.getStrCompiledJavaJarFile());
            jar(buildDir, paths, output, new BuildRequest<?, ?, ?, ?>[] { /* compileJavaCode */});
        }

        if(context.settings.format() == Format.jar) {
            final File buildDir = FileUtils.toFile(context.settings.getStrJavaDirectory());
            final String[] paths = new String[] { context.settings.getStrJavaTransDirectory().getName().getPath() };
            final File output = FileUtils.toFile(context.settings.getStrCompiledJarFile());
            jar(buildDir, paths, output, new BuildRequest<?, ?, ?, ?>[] { /* compileJavaCode */});
        }

        return None.val;
    }

    private void jar(File buildDir, String[] paths, File output, BuildRequest<?, ?, ?, ?>[] requirements)
        throws IOException {
        Map<File, Set<File>> files = new HashMap<>();
        Set<File> relativeFiles = new HashSet<>();
        Set<File> absoluteFiles = new HashSet<>();
        for(int i = 0; i < paths.length; i++) {
            if(FileCommands.acceptableAsAbsolute(paths[i]))
                absoluteFiles.add(new File(paths[i]));
            else
                relativeFiles.add(new File(buildDir, paths[i]));
        }
        files.put(buildDir, relativeFiles);
        files.put(new File(""), absoluteFiles);

        requireBuild(JavaJar.factory, new JavaJar.Input(JavaJar.Mode.CreateOrUpdate, output, null, files, requirements));
    }
}
