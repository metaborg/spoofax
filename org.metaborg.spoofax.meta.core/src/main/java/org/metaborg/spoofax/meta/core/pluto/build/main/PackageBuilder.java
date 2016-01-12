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
import org.metaborg.spoofax.meta.core.pluto.build.PackSdf;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Rtg.Input;
import org.metaborg.spoofax.meta.core.pluto.build.Sdf2Table;
import org.metaborg.spoofax.meta.core.pluto.build.misc.Copy;
import org.metaborg.spoofax.meta.core.pluto.stamp.DirectoryLastModifiedStamper;
import org.metaborg.util.cmd.Arguments;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JarBuilder;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

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
            jar(output, baseDir, null, settings.getStrCompiledJavaStrategiesDirectory(),
                settings.getDsGeneratedInterpreterCompiledJava(), settings.getDsManualInterpreterCompiledJava());
        }

        if(settings.format() == Format.jar) {
            // Copy .pp.af and .tbl to trans, so that they get included in the JAR file. Required for being able
            // to load those files from Stratego code.
            // TODO: extract build request/origin creation for these files into separate class to prevent code dup.
            final String sdfModule = settings.sdfName();
            final Arguments sdfArgs = GenerateSourcesBuilder.sdfArgs(context);
            final PackSdf.Input packSdfInput = GenerateSourcesBuilder.packSdfInput(context, sdfModule, sdfArgs);
            final Origin packSdf = PackSdf.origin(packSdfInput);

            final Origin.Builder originBuilder = Origin.Builder();
            final FileObject target = settings.getStrCompiledJavaTransDirectory();

            final PPPack.Input ppPackInput = GenerateSourcesBuilder.ppPackInput(context, sdfModule, packSdf);
            final File ppAfFile = requireBuild(PPPack.factory, ppPackInput).val;
            final File targetPpAfFile = toFile(target.resolveFile(settings.getPpAfName(sdfModule)));
            originBuilder.add(Copy.origin(new Copy.Input(ppAfFile, targetPpAfFile, Origin.from(PPPack
                .request(ppPackInput)), context.baseDir, context.depDir)));

            final PPGen.Input ppGenInput = GenerateSourcesBuilder.ppGenInput(context, sdfModule, packSdf);
            final File ppGenFile = requireBuild(PPGen.factory, ppGenInput).val;
            final File targetGenPpAfFile = toFile(target.resolveFile(settings.getGenPpAfName(sdfModule)));
            originBuilder.add(Copy.origin(new Copy.Input(ppGenFile, targetGenPpAfFile, Origin.from(PPGen
                .request(ppGenInput)), context.baseDir, context.depDir)));

            final Sdf2Table.Input sdf2TableInput =
                GenerateSourcesBuilder.sdf2TableInput(context, sdfModule, packSdfInput);
            final File tblFile = requireBuild(Sdf2Table.factory, sdf2TableInput).val;
            final File targeTblFile = toFile(target.resolveFile(settings.getSdfTableName(sdfModule)));
            originBuilder.add(Copy.origin(new Copy.Input(tblFile, targeTblFile, Origin.from(Sdf2Table
                .request(sdf2TableInput)), context.baseDir, context.depDir)));

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
            final File pathFile = toFile(path);
            require(pathFile, new DirectoryLastModifiedStamper());
            final FileObject[] files = path.findFiles(new AllFileSelector());
            if(files == null) {
                continue;
            }
            for(FileObject file : files) {
                final File javaFile = toFile(file);
                final String relative = relativize(file, baseDir);
                if(relative != null) { // Ignore files that are not relative to the base directory.
                    fileEntries.add(new JarBuilder.Entry(relative, javaFile));
                }
            }
        }

        final File jarFile = toFile(jarPath);

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
