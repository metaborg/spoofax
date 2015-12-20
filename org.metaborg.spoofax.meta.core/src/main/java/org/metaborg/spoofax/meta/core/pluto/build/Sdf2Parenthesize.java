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
import org.metaborg.spoofax.meta.core.pluto.build.aux.ParseSdfDefinition;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2ParenthesizeStamper;
import org.metaborg.spoofax.meta.core.pluto.util.LoggingFilteringIOAgent;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 6177130857266733408L;

        public final String sdfModule;

        public Input(SpoofaxContext context, String sdfModule) {
            super(context);
            this.sdfModule = sdfModule;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory = SpoofaxBuilderFactoryFactory.of(
        Sdf2Parenthesize.class, Input.class);


    public Sdf2Parenthesize(Input input) {
        super(input);
    }


    @Override protected String description(Input input) {
        return "Extract parenthesis structure from grammar";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2parenthesize." + input.sdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        final BuildRequest<PackSdf.Input, None, PackSdf, ?> packSdf =
            new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, Joiner.on(' ').join(
                context.settings.sdfArgs())));
        requireBuild(packSdf);

        final File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File outputPath = FileUtils.toFile(context.settings.getStrCompiledParenthesizerFile(input.sdfModule));
        final String outputmodule = "include/" + input.sdfModule + "-parenthesize";

        if(SpoofaxContext.BETTER_STAMPERS) {
            BuildRequest<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition =
                new BuildRequest<>(ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath,
                    new BuildRequest<?, ?, ?, ?>[] { packSdf }));
            require(inputPath, new Sdf2ParenthesizeStamper(parseSdfDefinition));
        } else
            require(inputPath);

        // TODO: avoid redundant call to sdf2table
        // TODO: set nativepath to the native bundle, so that sdf2table can be found
        final ExecutionResult result =
            StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), main_sdf2parenthesize_0_0.instance,
                "sdf2parenthesize", new LoggingFilteringIOAgent(Pattern.quote("[ sdf2parenthesize | info ]") + ".*",
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
