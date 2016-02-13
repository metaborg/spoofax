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
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseFile;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2ParenthesizeStamper;
import org.metaborg.util.cmd.Arguments;
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

        public final File inputPath;
        public final File outputPath;
        public final String outputModule;
        public final String sdfModule;

        public final Origin origin;


        public Input(SpoofaxContext context, File inputPath, File outputPath, String outputModule, String sdfModule,
            Origin origin) {
            super(context);
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.outputModule = outputModule;
            this.sdfModule = sdfModule;
            this.origin = origin;
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
        requireBuild(input.origin);

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<ParseFile.Input, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseFile.request(new ParseFile.Input(context, input.inputPath, input.origin));
            require(input.inputPath, new Sdf2ParenthesizeStamper(parseSdf));
        } else {
            require(input.inputPath);
        }

        // @formatter:off
        // TODO: set nativepath to the native bundle, so that sdf2table can be found?
        final Arguments arguments = new Arguments()
            .addFile("-i", input.inputPath)
            .add("-m", input.sdfModule)
            .add("--lang", input.sdfModule)
            .add("--omod", input.outputModule)
            .addFile("-o", input.outputPath)
            .add("--main-strategy", "io-" + input.sdfModule + "-parenthesize")
            .add("--rule-prefix", input.sdfModule)
            .add( "--sig-module", SpoofaxConstants.DIR_INCLUDE + "/" + input.sdfModule)
            ;
        
        final ExecutionResult result = new StrategoExecutor()
            .withToolsContext()
            .withStrategy(main_sdf2parenthesize_0_0.instance)
            .withTracker(newResourceTracker(Pattern.quote("[ sdf2parenthesize | info ]") + ".*", Pattern.quote("Invoking native tool") + ".*"))
            .withName("sdf2parenthesize")
            .executeCLI(arguments)
            ;
        
        if(!result.success) {
            FileCommands.writeToFile(input.outputPath, "module include/" + input.sdfModule + "-parenthesize rules parenthesize-" + input.sdfModule + " = id");
        }
        // @formatter:on 

        provide(input.outputPath);

        return None.val;
    }
}
