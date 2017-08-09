package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_pack_sdf_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.FileExistsStamper;

public class PackSdf extends SpoofaxBuilder<PackSdf.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 2058684747897720328L;

        public final String module;
        public final File inputFile;
        public final File outputFile;
        public final List<File> includePaths;
        public final Arguments extraArgs;
        public final @Nullable Origin origin;


        public Input(SpoofaxContext context, String module, File inputFile, File outputFile, List<File> includePaths,
            Arguments extraArgs, @Nullable Origin origin) {
            super(context);
            this.module = module;
            this.extraArgs = extraArgs;
            this.inputFile = inputFile;
            this.includePaths = includePaths;
            this.outputFile = outputFile;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, PackSdf> factory =
        SpoofaxBuilderFactoryFactory.of(PackSdf.class, Input.class);


    public PackSdf(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, PackSdf, SpoofaxBuilderFactory<Input, OutputPersisted<File>, PackSdf>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Pack SDF modules";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("pack-sdf." + input.module + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        require(input.inputFile);

        if(input.origin != null) {
            requireBuild(input.origin);
        }
        
        final Arguments args = new Arguments();
        args.addAll(input.extraArgs);
        for(File path : input.includePaths) {
            require(path, FileExistsStamper.instance);
            if(!path.exists()) {
                continue;
            }
            if(FilenameUtils.isExtension(path.getName(), "def")) {
                args.addFile("-Idef", path);
            } else {
                /*
                 * HACK: for full incremental soundness, a require on the directory is needed here, since new files can
                 * be added to the path, which influence pack-sdf. However, since the Spoofax build generates new files
                 * into some of these directories, that would cause the requirement to always be inconsistent, always
                 * triggering a rebuild. This is why we omit the requirement.
                 * 
                 * seba: This could be solved using a customary stamper that only tracks files matching some naming
                 * convention.
                 */
                args.addFile("-I", path);
            }
        }

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .addFile("-o", input.outputFile)
            .addAll(args)
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_pack_sdf_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("  including ") + ".*"))
            .withName("pack-sdf")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputFile);
        for(File required : extractRequiredPaths(result.errLog)) {
            require(required);
        }

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputFile);
    }


    private List<File> extractRequiredPaths(String log) {
        final String prefix = "  including ";
        final String infix = " from ";

        List<File> paths = new ArrayList<>();
        for(String s : log.split("\\n")) {
            if(s.startsWith(prefix)) {
                String module = s.substring(prefix.length());
                int infixIndex = module.indexOf(infix);
                if(infixIndex < 0 && FileCommands.acceptableAsAbsolute(module)) {
                    paths.add(new File(s.substring(prefix.length())));
                } else if(infixIndex >= 0) {
                    String def = module.substring(infixIndex + infix.length());
                    if(FileCommands.acceptable(def)) {
                        paths.add(new File(def));
                    }
                }
            }
        }
        return paths;
    }
}
