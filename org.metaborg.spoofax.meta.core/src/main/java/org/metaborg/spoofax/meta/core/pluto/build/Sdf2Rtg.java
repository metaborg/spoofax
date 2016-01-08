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
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2RtgStamper;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2rtg_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Sdf2Rtg extends SpoofaxBuilder<Sdf2Rtg.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4487049822305558202L;

        public final File inputPath;
        public final File outputPath;
        public final String sdfModule;

        public final Origin origin;


        public Input(SpoofaxContext context, File inputPath, File outputPath, String sdfModule, Origin origin) {
            super(context);
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.sdfModule = sdfModule;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Rtg> factory = SpoofaxBuilderFactoryFactory
        .of(Sdf2Rtg.class, Input.class);


    public Sdf2Rtg(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, Sdf2Rtg, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Rtg>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Extract constructor signatures from grammar";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2rtg." + input.sdfModule + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<ParseFile.Input, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseFile.request(new ParseFile.Input(context, input.inputPath, input.origin));
            require(input.inputPath, new Sdf2RtgStamper(parseSdf));
        } else {
            require(input.inputPath);
        }

        // @formatter:off
        // TODO: set nativepath to the native bundle, so that sdf2table can be found?
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputPath)
            .addAll("-m", input.sdfModule)
            .addFile("-o", input.outputPath)
            .add("--ignore-missing-cons")
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_sdf2rtg_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("Invoking native tool") + ".*"))
            .withName("sdf2rtg")
            .executeCLI(arguments)
            ;
        // @formatter:on 

        provide(input.outputPath);

        setState(State.finished(result.success));
        return OutputPersisted.of(input.outputPath);
    }
}
