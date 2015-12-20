package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.util.LoggingFilteringIOAgent;
import org.metaborg.util.file.FileUtils;
import org.strategoxt.permissivegrammars.make_permissive;

import build.pluto.BuildUnit.State;
import build.pluto.output.None;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 4381601872931676757L;

        public final String sdfModule;
        public final String sdfArgs;


        public Input(SpoofaxContext context, String sdfModule, String sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, MakePermissive> factory = SpoofaxBuilderFactoryFactory.of(
        MakePermissive.class, Input.class);


    public MakePermissive(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Make grammar permissive for error-recovery parsing.";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("make-permissive." + context.settings.sdfName() + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, input.sdfArgs));

        final File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File outputPath = FileUtils.toFile(context.settings.getSdfCompiledPermissiveDefFile(input.sdfModule));

        require(inputPath);

        final ExecutionResult result =
            StrategoExecutor.runStrategoCLI(StrategoExecutor.permissiveGrammarsContext(),
                make_permissive.getMainStrategy(), "make-permissive",
                new LoggingFilteringIOAgent(Pattern.quote("[ make-permissive | info ]") + ".*"), "-i", inputPath, "-o",
                outputPath, "--optimize", "on");

        provide(outputPath);
        setState(State.finished(result.success));

        return None.val;
    }
}
