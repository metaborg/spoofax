package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.SpoofaxConstants;
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
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileHashStamper;
import mb.stratego.compiler.pack.Packer;

public class StrIncrBackEnd extends SpoofaxBuilder<StrIncrBackEnd.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 7275406432476758521L;

        public final Origin frontEndTasks;
        public final @Nullable String strategyName;
        public final File strategyDir;
        public final Collection<File> strategyContributions;
        public final String packageName;
        public final File outputPath;
        public final File cacheDir;
        public final Arguments extraArgs;
        public final boolean isBoilerplate;

        public Input(SpoofaxContext context, Origin frontEndTasks, @Nullable String strategyName, File strategyDir,
            Collection<File> strategyContributions, String packageName, File outputPath, File cacheDir,
            Arguments extraArgs, boolean isBoilerplate) {
            super(context);

            this.frontEndTasks = frontEndTasks;
            this.strategyName = strategyName;
            this.strategyDir = strategyDir;
            this.strategyContributions = strategyContributions;
            this.packageName = packageName;
            this.outputPath = outputPath;
            this.cacheDir = cacheDir;
            this.extraArgs = extraArgs;
            this.isBoilerplate = isBoilerplate;
        }

        @Override public String toString() {
            return "StrIncrFrontEnd$Input(" + strategyName + ", ... )";
        }
    }

    // Just a type alias
    public static class BuildRequest extends
        build.pluto.builder.BuildRequest<Input, None, StrIncrBackEnd, SpoofaxBuilderFactory<Input, None, StrIncrBackEnd>> {
        private static final long serialVersionUID = -1299552527869341531L;

        public BuildRequest(SpoofaxBuilderFactory<Input, None, StrIncrBackEnd> factory, Input input) {
            super(factory, input);
        }
    }

    public static SpoofaxBuilderFactory<Input, None, StrIncrBackEnd> factory =
        SpoofaxBuilderFactoryFactory.of(StrIncrBackEnd.class, Input.class);

    public static BuildRequest request(Input input) {
        return new BuildRequest(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    public StrIncrBackEnd(Input input) {
        super(input);
    }

    @Override protected None build(Input input) throws Throwable {
        requireBuild(input.frontEndTasks);

        for(File strategyContrib : input.strategyContributions) {
            require(strategyContrib, FileHashStamper.instance);
        }

        // Pack the directory into a single strategy
        Path packedFile = Paths.get(input.strategyDir.toString(), "packed$.ctree");
        if(input.isBoilerplate) {
            Packer.packBoilerplate(input.strategyDir.toPath(), packedFile);
        } else {
            Packer.packStrategy(input.strategyDir.toPath(), packedFile, input.strategyName);
        }

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        final Arguments arguments = new Arguments().addFile("-i", packedFile.toFile()).addFile("-o", input.outputPath)
            .addLine(input.packageName != null ? "-p " + input.packageName : "").add("--library").add("--fusion");
        if(input.isBoilerplate) {
            arguments.add("--boilerplate");
        } else {
            arguments.add("--single-strategy");
        }

        if(input.cacheDir != null) {
            arguments.addFile("--cache-dir", input.cacheDir);
        }
        arguments.addAll(input.extraArgs);

        final ResourceAgentTracker tracker = newResourceTracker(Pattern.quote("[ strj | info ]") + ".*",
            Pattern.quote("[ strj | error ] Compilation failed") + ".*",
            Pattern.quote("[ strj | warning ] Nullary constructor") + ".*",
            Pattern.quote("[ strj | warning ] No Stratego files found in directory") + ".*",
            Pattern.quote("[ strj | warning ] Found more than one matching subdirectory found for") + ".*",
            Pattern.quote(SpoofaxConstants.STRJ_INFO_WRITING_FILE) + ".*",
            Pattern.quote("* warning (escaping-var-id):") + ".*",
            Pattern.quote("          [\"") + ".*" + Pattern.quote("\"]"));

        final ExecutionResult result =
            new StrategoExecutor().withStrjContext().withStrategy(org.strategoxt.strj.main_0_0.instance)
                .withTracker(tracker).withName("strj").setSilent(true).executeCLI(arguments);

        Arrays.stream(result.errLog.split(System.lineSeparator()))
            .filter(line -> line.startsWith(SpoofaxConstants.STRJ_INFO_WRITING_FILE))
            .forEach(line -> {
                String fileName = line.substring(SpoofaxConstants.STRJ_INFO_WRITING_FILE.length());
                provide(new File(fileName));
            });

        setState(State.finished(result.success));
        return None.val;
    }

    @Override protected String description(Input input) {
        if(input.strategyName == null) {
            return "Combine and compile separate strategy ast files to Java file: main / interopregistrer";
        }
        return "Combine and compile separate strategy ast files to Java file: " + input.strategyName;
    }

    @Override public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.strategyDir);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("str_sep_back." + relname + ".dep");
    }

}
