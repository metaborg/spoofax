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
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseFile;
import org.metaborg.spoofax.meta.core.pluto.stamp.PPGenStamper;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

public class PPGen extends SpoofaxBuilder<PPGen.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6752720592940603183L;

        public final String sdfModule;
        public final Arguments sdfArgs;


        public Input(SpoofaxContext context, String sdfModule, Arguments sdfArgs) {
            super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
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

        final Origin packSdf = PackSdf.origin(new PackSdf.Input(context, input.sdfModule, input.sdfArgs));
        requireBuild(packSdf);

        final File inputPath = toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File ppOutputPath = toFile(context.settings.getGenPpCompiledFile(input.sdfModule));
        final File afOutputPath = toFile(context.settings.getGenPpAfCompiledFile(input.sdfModule));

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<ParseFile.Input, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseFile.request(new ParseFile.Input(context, inputPath, packSdf));
            require(inputPath, new PPGenStamper(parseSdf));
        } else {
            require(inputPath);
        }

        // @formatter:off
        final Arguments afArguments = new Arguments()
            .addFile("-i", inputPath)
            .add("-t")
            .add("-b")
            .addFile("-o", afOutputPath)
            ;

        final ResourceAgentTracker tracker = newResourceTracker(
            Pattern.quote("[ pp-gen | warning ]") + ".*"
          , Pattern.quote("{prefer,") + ".*"
        );

        final ExecutionResult afResult = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_ppgen_0_0.instance)
            .withTracker(tracker)
            .withName("pp-gen")
            .executeCLI(afArguments)
            ;
        // @formatter:on 

        provide(afOutputPath);

        // @formatter:off
        final Arguments ppArguments = new Arguments()
            .addFile("-i", afOutputPath)
            .addFile("-o", ppOutputPath)
            ;

        final ExecutionResult ppResult = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_pp_pp_table_0_0.instance)
            .withTracker(newResourceTracker())
            .withName("pp-table")
            .executeCLI(ppArguments)
            ;
        // @formatter:on 

        provide(ppOutputPath);

        if(!FileCommands.exists(afOutputPath)) {
            FileCommands.writeToFile(afOutputPath, "PP-Table([])");
            provide(afOutputPath);
        }

        setState(State.finished(afResult.success && ppResult.success));
        return None.val;
    }
}
