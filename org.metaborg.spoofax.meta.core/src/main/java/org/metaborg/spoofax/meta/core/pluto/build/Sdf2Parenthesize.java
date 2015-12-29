package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseSdf;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2ParenthesizeStamper;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 6177130857266733408L;

        public final String sdfModule;
        public final String sdfArgs;

        public Input(SpoofaxContext context, String sdfModule, String sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory = SpoofaxBuilderFactoryFactory.of(
        Sdf2Parenthesize.class, Input.class);


    public Sdf2Parenthesize(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, Sdf2Parenthesize, SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Extract parenthesis structure from grammar";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2parenthesize." + input.sdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        final Origin packSdf = PackSdf.origin(new PackSdf.Input(context, input.sdfModule, input.sdfArgs));
        requireBuild(packSdf);

        final File inputPath = toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File outputPath = toFile(context.settings.getStrCompiledParenthesizerFile(input.sdfModule));
        final String outputmodule = "include/" + input.sdfModule + "-parenthesize";

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseSdf.request(new ParseSdf.Input(context, inputPath, packSdf));
            require(inputPath, new Sdf2ParenthesizeStamper(parseSdf));
        } else
            require(inputPath);

        // TODO: avoid redundant call to sdf2table
        // TODO: set nativepath to the native bundle, so that sdf2table can be found
        final ExecutionResult result =
            StrategoExecutor.runStrategoCLI(
                StrategoExecutor.toolsContext(),
                main_sdf2parenthesize_0_0.instance,
                "sdf2parenthesize",
                newResourceTracker(Pattern.quote("[ sdf2parenthesize | info ]") + ".*",
                    Pattern.quote("Invoking native tool") + ".*"), "-i", inputPath, "-m", input.sdfModule, "--lang",
                input.sdfModule, "--omod", outputmodule, "-o", outputPath, "--main-strategy", "io-" + input.sdfModule
                    + "-parenthesize", "--rule-prefix", input.sdfModule, "--sig-module", SpoofaxConstants.DIR_INCLUDE
                    + "/" + input.sdfModule);

        if(!result.success) {
            FileCommands.writeToFile(outputPath, "module include/" + input.sdfModule
                + "-parenthesize rules parenthesize-" + input.sdfModule + " = id");
        }

        provide(outputPath);

        return None.val;
    }
}
