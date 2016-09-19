package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2ParenthesizeStamper;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 6177130857266733408L;

        public final File inputFile;
        public final File outputFile;
        public final String inputModule;
        public final String outputModule;
        public final @Nullable Origin origin;


        public Input(SpoofaxContext context, File inputFile, File outputFile, String inputModule, String outputModule,
            @Nullable Origin origin) {
            super(context);
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.inputModule = inputModule;
            this.outputModule = outputModule;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2Parenthesize.class, Input.class);


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
        return context.depPath("sdf2parenthesize." + input.inputModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        requireBuild(input.origin);

        if(SpoofaxContext.BETTER_STAMPERS) {
            require(input.inputFile, new Sdf2ParenthesizeStamper(input.context));
        } else {
            require(input.inputFile);
        }

        // @formatter:off
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputFile)
            .add("-m", input.inputModule)
            .add("--lang", input.inputModule)
            .add("--omod", input.outputModule)
            .addFile("-o", input.outputFile)
            .add("--main-strategy", "io-" + input.inputModule + "-parenthesize")
            .add("--rule-prefix", input.inputModule)
            .add( "--sig-module", "signatures/-")
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_sdf2parenthesize_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("[ sdf2parenthesize | info ]") + ".*", Pattern.quote("Invoking native tool") + ".*"))
            .withName("sdf2parenthesize")
            .executeCLI(arguments)
            ;
        
        if(!result.success) {
            FileCommands.writeToFile(input.outputFile, "module " + input.inputModule + "\nrules\n\n  parenthesize-" + input.inputModule + " = id");
        }
        // @formatter:on 

        provide(input.outputFile);

        return None.val;
    }
}
