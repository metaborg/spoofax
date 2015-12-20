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
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseSdf;
import org.metaborg.spoofax.meta.core.pluto.stamp.PPGenStamper;
import org.metaborg.spoofax.meta.core.pluto.util.LoggingFilteringIOAgent;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class PPGen extends SpoofaxBuilder<PPGen.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6752720592940603183L;

        public final String sdfModule;


        public Input(SpoofaxContext context, String sdfModule) {
            super(context);
            this.sdfModule = sdfModule;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, PPGen> factory = SpoofaxBuilderFactoryFactory.of(PPGen.class,
        PPGen.Input.class);


    public PPGen(Input context) {
        super(context);
    }


    public static BuildRequest<Input, None, PPGen, SpoofaxBuilderFactory<Input, None, PPGen>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Generate pretty-print table from grammar";
    }

    @Override public File persistentPath(Input input) {
        return input.context.depPath("pp-gen." + input.sdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        if(!context.isBuildStrategoEnabled(this)) {
            return None.val;
        }

        final String sdfArgs = Joiner.on(' ').join(context.settings.sdfArgs());
        final Origin packSdf = PackSdf.origin(new PackSdf.Input(context, input.sdfModule, sdfArgs));
        requireBuild(packSdf);

        final File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File ppOutputPath = FileUtils.toFile(context.settings.getGenPpCompiledFile(input.sdfModule));
        final File afOutputPath = FileUtils.toFile(context.settings.getGenPpAfCompiledFile(input.sdfModule));

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseSdf.request(new ParseSdf.Input(context, inputPath, packSdf));
            require(inputPath, new PPGenStamper(parseSdf));
        } else {
            require(inputPath);
        }

        final ExecutionResult afResult =
            StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), main_ppgen_0_0.instance, "main-ppgen",
                new LoggingFilteringIOAgent(Pattern.quote("[ main-ppgen | warning ]") + ".*"), "-i", inputPath, "-t",
                "-b", "-o", afOutputPath);
        provide(afOutputPath);

        // TODO: not needed to require something that was provided before?
        // require(afOutputPath);
        final ExecutionResult ppResult =
            StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), main_pp_pp_table_0_0.instance,
                "main-pp-pp-table", new LoggingFilteringIOAgent(), "-i", afOutputPath, "-o", ppOutputPath);
        provide(ppOutputPath);

        if(!FileCommands.exists(afOutputPath)) {
            FileCommands.writeToFile(afOutputPath, "PP-Table([])");
            provide(afOutputPath);
        }

        setState(State.finished(afResult.success && ppResult.success));

        return None.val;
    }
}
