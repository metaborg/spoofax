package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_pack_sdf_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;

public class PackSdf extends SpoofaxBuilder<PackSdf.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 2058684747897720328L;

        public final String sdfModule;
        public final Arguments sdfArgs;
        public final @Nullable File externalDef;
        public final File inputPath;
        public final File outputPath;


        public Input(SpoofaxContext context, String sdfModule, Arguments sdfArgs, @Nullable File externalDef,
            File inputPath, File outputPath) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
            this.externalDef = externalDef;
            this.inputPath = inputPath;
            this.outputPath = outputPath;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, PackSdf> factory = SpoofaxBuilderFactoryFactory
        .of(PackSdf.class, Input.class);


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
        return context.depPath("pack-sdf." + input.sdfModule + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        if(input.externalDef != null) {
            require(input.externalDef, LastModifiedStamper.instance);
            FileCommands.copyFile(input.externalDef, input.outputPath, StandardCopyOption.COPY_ATTRIBUTES);
            provide(input.outputPath);
            return OutputPersisted.of(input.outputPath);
        }

        copySdf2();

        require(input.inputPath);

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputPath)
            .addFile("-o", input.outputPath)
            .addAll(input.sdfArgs)
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_pack_sdf_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("  including ") + ".*"))
            .withName("pack-sdf")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputPath);
        for(File required : extractRequiredPaths(result.errLog)) {
            require(required);
        }

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputPath);
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
                    if(FileCommands.acceptable(def))
                        paths.add(new File(def));
                }
            }
        }
        return paths;
    }

    /**
     * Copy SDF2 files from syntax/ to src-gen/syntax, to support projects that do not use SDF3.
     */
    private void copySdf2() {
        final File syntaxDir = toFile(context.settings.getSyntaxDirectory());
        final File genSyntaxDir = toFile(context.settings.getGenSyntaxDirectory());

        // TODO: identify sdf2 files using Spoofax core
        List<Path> srcSdfFiles = FileCommands.listFilesRecursive(syntaxDir.toPath(), new SuffixFileFilter("sdf"));
        for(Path p : srcSdfFiles) {
            require(p.toFile(), LastModifiedStamper.instance);
            File target =
                FileCommands.copyFile(syntaxDir, genSyntaxDir, p.toFile(), StandardCopyOption.COPY_ATTRIBUTES);
            provide(target);
        }
    }
}
