package org.metaborg.spoofax.meta.core.pluto.build.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.PPGen;
import org.metaborg.spoofax.meta.core.pluto.build.PPPack;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg.Input;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.misc.Copy;
import org.metaborg.util.file.FileUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class PackageBuilder extends SpoofaxBuilder<SpoofaxInput, None> {
    public static SpoofaxBuilderFactory<SpoofaxInput, None, PackageBuilder> factory = SpoofaxBuilderFactoryFactory.of(
        PackageBuilder.class, SpoofaxInput.class);


    public PackageBuilder(SpoofaxInput input) {
        super(input);
    }


    public static
        BuildRequest<SpoofaxInput, None, PackageBuilder, SpoofaxBuilderFactory<SpoofaxInput, None, PackageBuilder>>
        request(SpoofaxInput input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(SpoofaxInput input) {
        return "Package";
    }

    @Override public File persistentPath(SpoofaxInput input) {
        return context.depPath("package.dep");
    }

    @Override protected None build(SpoofaxInput input) throws Throwable {
        final SpoofaxProjectSettings settings = context.settings;

        // TODO: build Java code with Pluto.
        // BuildRequest<CompileJavaCode.Input, None, CompileJavaCode, ?> compileJavaCode =
        // new BuildRequest<>(CompileJavaCode.factory, new CompileJavaCode.Input(context,
        // new BuildRequest<?, ?, ?, ?>[] { strategoCtree }));
        // requireBuild(compileJavaCode);

        final FileObject baseDir = settings.getOutputClassesDirectory();
        if(context.isJavaJarEnabled(this)) {
            final FileObject output = settings.getStrCompiledJavaJarFile();
            // TODO: get javajar-includes from project settings?
            // String[] paths = context.props.getOrElse("javajar-includes",
            // context.settings.packageStrategiesPath()).split("[\\s]+");
            jar(output, baseDir, null, settings.getStrCompiledJavaStrategiesDirectory());
        }

        if(settings.format() == Format.jar) {
            // Copy .pp.af and .tbl to trans, so that they get included in the JAR file. Required for being able
            // to load those files from Stratego code.
            // TODO: extract build request/origin creation for these files into separate class to prevent code dup.
            final String sdfModule = settings.sdfName();
            final String sdfArgs = Joiner.on(' ').join(settings.sdfArgs());

            final Origin.Builder originBuilder = Origin.Builder();
            final FileObject target = settings.getStrCompiledJavaTransDirectory();

            final File ppPackInputPath = FileUtils.toFile(settings.getPpFile(sdfModule));
            final File ppPackOutputPath = FileUtils.toFile(settings.getPpAfCompiledFile(sdfModule));
            final File ppAfFile = FileUtils.toFile(settings.getPpAfCompiledFile(sdfModule));
            final File targetPpAfFile = FileUtils.toFile(target.resolveFile(settings.getPpAfName(sdfModule)));
            final Origin ppAfOrigin = PPPack.origin(new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, true));
            originBuilder.add(Copy.origin(new Copy.Input(ppAfFile, targetPpAfFile, ppAfOrigin, context.baseDir,
                context.depDir)));

            final File genPpAfFile = FileUtils.toFile(settings.getGenPpAfCompiledFile(sdfModule));
            final File targetGenPpAfFile = FileUtils.toFile(target.resolveFile(settings.getGenPpAfName(sdfModule)));
            final Origin genPpAfOrigin = PPGen.origin(new PPGen.Input(context, sdfModule));
            originBuilder.add(Copy.origin(new Copy.Input(genPpAfFile, targetGenPpAfFile, genPpAfOrigin,
                context.baseDir, context.depDir)));

            final File tblFile = FileUtils.toFile(settings.getSdfCompiledTableFile(sdfModule));
            final File targeTblFile = FileUtils.toFile(target.resolveFile(settings.getSdfTableName(sdfModule)));
            final Origin tblOrigin = Sdf2Table.origin(new Sdf2Table.Input(context, sdfModule, sdfArgs));
            originBuilder.add(Copy.origin(new Copy.Input(tblFile, targeTblFile, tblOrigin, context.baseDir,
                context.depDir)));

            final Origin origin = originBuilder.get();
            requireBuild(origin);

            // Jar
            final FileObject output = settings.getStrCompiledJarFile();
            jar(output, baseDir, origin, target);
        }

        return None.val;
    }

    public void jar(FileObject jarPath, FileObject baseDir, @Nullable Origin origin, FileObject... paths)
        throws IOException {
        final Collection<JarBuilder.Entry> fileEntries = Lists.newLinkedList();

        for(FileObject path : paths) {
            final FileObject[] files = path.findFiles(new AllFileSelector());
            if(files == null) {
                continue;
            }
            for(FileObject file : files) {
                final File javaFile = FileUtils.toFile(file);
                final String relative = relativize(file, baseDir);
                if(relative != null) { // Ignore files that are not relative to the base directory.
                    fileEntries.add(new JarBuilder.Entry(relative, javaFile));
                }
            }
        }

        final File jarFile = FileUtils.toFile(jarPath);

        requireBuild(JarBuilder.factory, new JarBuilder.Input(jarFile, fileEntries, origin));
    }

    private @Nullable String relativize(FileObject path, FileObject base) throws FileSystemException {
        final FileName pathName = path.getName();
        final FileName baseName = base.getName();
        if(!baseName.isDescendent(pathName)) {
            return null;
        }
        return baseName.getRelativeName(pathName);
    }
}
