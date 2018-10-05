package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class Strj extends SpoofaxBuilder<Strj.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -5234502421638344690L;

        public final File inputFile;
        public final File outputPath;
        public final File depPath;
        public final String packageName;
        public final boolean library;
        public final boolean clean;
        public final List<File> includeDirs;
        public final List<File> includeFiles;
        public final List<String> includeLibs;
        public final File cacheDir;
        public final Arguments extraArgs;
        public final Origin origin;


        public Input(SpoofaxContext context, File inputPath, File outputPath, File depPath, String packageName,
            boolean library, boolean clean, List<File> includeDirs, List<File> includeFiles, List<String> includeLibs,
            File cacheDir, Arguments extraArgs, Origin origin) {
            super(context);
            this.inputFile = inputPath;
            this.outputPath = outputPath;
            this.depPath = depPath;
            this.packageName = packageName;
            this.library = library;
            this.clean = clean;
            this.includeDirs = includeDirs;
            this.includeFiles = includeFiles;
            this.includeLibs = includeLibs;
            this.cacheDir = cacheDir;
            this.extraArgs = extraArgs;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Strj> factory =
        SpoofaxBuilderFactoryFactory.of(Strj.class, Input.class);


    public Strj(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, Strj, SpoofaxBuilderFactory<Input, None, Strj>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compile Stratego code";
    }

    @Override public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.inputFile);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("strj." + relname + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(input.origin);

        require(input.inputFile);

        final File rtree = FileCommands.replaceExtension(input.outputPath, "rtree");
        final File strdep = FileCommands.addExtension(input.outputPath, "dep");

        // @formatter:off            
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .addFile("-o", input.outputPath)
            .addLine(input.packageName != null ? "-p " + input.packageName : "")
            .add(input.library ? "--library" : "")
            .add(input.clean ? "--clean" : "")
            ;
        // @formatter:on

        for(File dir : input.includeDirs) {
            if(dir != null) {
                arguments.addFile("-I", dir);
            }
        }
        for(File file : input.includeFiles) {
            if(file != null) {
                arguments.addFile("-i", file);
            }
        }
        for(String lib : input.includeLibs) {
            if(lib != null && !lib.isEmpty()) {
                arguments.add("-la", lib);
            }
        }
        if(input.cacheDir != null) {
            arguments.addFile("--cache-dir", input.cacheDir);
        }
        arguments.addAll(input.extraArgs);

        // Delete rtree file to prevent it influencing the build.
        rtree.delete();

        // @formatter:off
        final ResourceAgentTracker tracker = newResourceTracker(
            Pattern.quote("[ strj | info ]") + ".*"
          , Pattern.quote("[ strj | error ] Compilation failed") + ".*"
          , Pattern.quote("[ strj | warning ] Nullary constructor") + ".*"
          , Pattern.quote("[ strj | warning ] No Stratego files found in directory") + ".*"
          , Pattern.quote("[ strj | warning ] Found more than one matching subdirectory found for") + ".*"
          , Pattern.quote("          [\"") + ".*" + Pattern.quote("\"]")
        );
        
        final ExecutionResult result = new StrategoExecutor()
            .withStrjContext()
            .withStrategy(org.strategoxt.strj.main_0_0.instance)
            .withTracker(tracker)
            .withName("strj")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        // Delete rtree file again to prevent it influencing subsequent builds.
        rtree.delete();

        if(input.depPath.isDirectory()) {
            for(Path sourceFile : FileCommands.listFilesRecursive(input.depPath.toPath())) {
                provide(sourceFile.toFile());
            }
        } else {
            provide(input.depPath);
        }
        provide(strdep);
        if(result.success) {
            if(FileCommands.exists(strdep)) {
                registerUsedPaths(strdep);
            }
        } else {
            // If Stratego compilation fails, the resulting .dep file is incomplete, so require all Stratego files.
            for(File sourceFile : FileUtils.listFiles(context.baseDir, new String[] { "str" }, true)) {
                require(sourceFile);
            }
        }

        setState(State.finished(result.success));
        return None.val;
    }

    private void registerUsedPaths(File strdep) throws IOException {
        final List<String> lines = org.apache.commons.io.FileUtils.readLines(strdep);

        // Skip first line (start at 1 instead of 0), which lists the generated CTree file.
        for(int i = 1; i < lines.size(); i++) {
            // Remove leading and trailing whitespace.
            final String trimmedLine = lines.get(i).trim();
            final int length = trimmedLine.length();
            if(length < 3) {
                // Don't process empty lines, i.e. lines with just ' /' or '/'.
                continue;
            }
            // Remove the trailing ' /'.
            final String line = trimmedLine.substring(0, length - 2);

            // TODO: non-local dependencies, such as those on .spoofax-language files, are copied to a temporary
            // directory. That will cause unnecessary rebuilds because of absolute path dependencies.
            final File file = new File(line);
            require(file);
        }
    }
}
