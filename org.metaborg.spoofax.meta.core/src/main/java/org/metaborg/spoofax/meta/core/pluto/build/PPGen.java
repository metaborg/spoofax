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
import build.pluto.output.OutputPersisted;

public class PPGen extends SpoofaxBuilder<PPGen.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6752720592940603183L;

        public final File inputPath;
        public final File ppOutputPath;
        public final File afOutputPath;
        public final String sdfModule;

        public final Origin origin;


        public Input(SpoofaxContext context, File inputPath, File ppOutputPath, File afOutputPath, String sdfModule,
            Origin origin) {
            super(context);
            this.inputPath = inputPath;
            this.ppOutputPath = ppOutputPath;
            this.afOutputPath = afOutputPath;
            this.sdfModule = sdfModule;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, PPGen> factory = SpoofaxBuilderFactoryFactory.of(
        PPGen.class, PPGen.Input.class);


    public PPGen(Input context) {
        super(context);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, PPGen, SpoofaxBuilderFactory<Input, OutputPersisted<File>, PPGen>>
        request(Input input) {
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

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<ParseFile.Input, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseFile.request(new ParseFile.Input(context, input.inputPath, input.origin));
            require(input.inputPath, new PPGenStamper(parseSdf));
        } else {
            require(input.inputPath);
        }

        // @formatter:off
        final Arguments afArguments = new Arguments()
            .addFile("-i", input.inputPath)
            .add("-t")
            .add("-b")
            .addFile("-o", input.afOutputPath)
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

        provide(input.afOutputPath);

        // @formatter:off
        final Arguments ppArguments = new Arguments()
            .addFile("-i", input.afOutputPath)
            .addFile("-o", input.ppOutputPath)
            ;

        final ExecutionResult ppResult = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_pp_pp_table_0_0.instance)
            .withTracker(newResourceTracker())
            .withName("pp-table")
            .executeCLI(ppArguments)
            ;
        // @formatter:on 

        provide(input.ppOutputPath);

        if(!FileCommands.exists(input.afOutputPath)) {
            FileCommands.writeToFile(input.afOutputPath, "PP-Table([])");
            provide(input.afOutputPath);
        }

        setState(State.finished(afResult.success && ppResult.success));
        return OutputPersisted.of(input.afOutputPath);
    }
}
