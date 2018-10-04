package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import build.pluto.dependency.Origin;

public class StrategoIncrementalFrontEnd
        extends SpoofaxBuilder<StrategoIncrementalFrontEnd.Input, StrategoIncrementalFrontEnd.Output> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 1548589152421064400L;

        public final File inputFile;
        public final String packageName;
        public final File cacheDir;
        public final Arguments extraArgs;
        public final Origin origin;

        public Input(SpoofaxContext context, File inputFile, String packageName, File cacheDir, Arguments extraArgs, Origin origin) {
            super(context);
            this.inputFile = inputFile;
            this.packageName = packageName;
            this.cacheDir = cacheDir;
            this.extraArgs = extraArgs;
            this.origin = origin;
        }
    }

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = 3808911543715367986L;

        public final BuildRequest request;
        public final String moduleName;
        public final List<String> seenStrategies;

        public Output(BuildRequest request, String moduleName, List<String> seenStrategies) {
            this.request = request;
            this.moduleName = moduleName;
            this.seenStrategies = seenStrategies;
        }
    }

    // Just a type alias
    public static class BuildRequest extends
            build.pluto.builder.BuildRequest<Input, Output, StrategoIncrementalFrontEnd, SpoofaxBuilderFactory<Input, Output, StrategoIncrementalFrontEnd>> {
        private static final long serialVersionUID = -1299552527869341531L;

        public BuildRequest(SpoofaxBuilderFactory<Input, Output, StrategoIncrementalFrontEnd> factory, Input input) {
            super(factory, input);
        }
    }

    public static SpoofaxBuilderFactory<Input, Output, StrategoIncrementalFrontEnd> factory = SpoofaxBuilderFactoryFactory
            .of(StrategoIncrementalFrontEnd.class, Input.class);

    public static BuildRequest request(Input input) {
        return new BuildRequest(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    public StrategoIncrementalFrontEnd(Input input) {
        super(input);
    }

    @Override
    protected Output build(Input input) throws Throwable {
        requireBuild(input.origin);

        require(input.inputFile);

        // TODO: implement
        String moduleName = null;
        List<String> seenStrategies = null;

        return new Output(request(input), moduleName, seenStrategies);
    }

    @Override
    protected String description(Input input) {
        return "Compile Stratego to separate strategy ast files";
    }

    @Override
    public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.inputFile);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("str_sep_front." + relname + ".dep");
    }
}
