package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.strategoxt.tools.main_parse_pp_table_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class PPPack extends SpoofaxBuilder<PPPack.Input, OutputPersisted<File>> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -5786344696509159033L;

        public final File inputPath;
        public final File outputPath;

        public final Origin origin;


        public Input(SpoofaxContext context, File ppInput, File ppTermOutput, Origin origin) {
            super(context);
            this.inputPath = ppInput;
            this.outputPath = ppTermOutput;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, PPPack> factory = SpoofaxBuilderFactoryFactory
        .of(PPPack.class, Input.class);


    public PPPack(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, PPPack, SpoofaxBuilderFactory<Input, OutputPersisted<File>, PPPack>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compress pretty-print table";
    }

    @Override public File persistentPath(Input input) {
        Path rel = FileCommands.getRelativePath(context.baseDir, input.outputPath);
        String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("pp-pack." + relname + ".dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);

        require(input.inputPath);
        if(!FileCommands.exists(input.inputPath)) {
            FileCommands.writeToFile(input.outputPath, "PP-Table([])");
            provide(input.outputPath);
        } else {
            // @formatter:off
            final Arguments arguments = new Arguments()
                .addFile("-i", input.inputPath)
                .addFile("-o", input.outputPath)
                ;
            
            final ExecutionResult result = new StrategoExecutor()
                .withToolsContext()
                .withStrategy(main_parse_pp_table_0_0.instance)
                .withTracker(newResourceTracker())
                .withName("parse-pp-table")
                .executeCLI(arguments)
                ;
            // @formatter:on 

            provide(input.outputPath);

            setState(State.finished(result.success));
        }

        return OutputPersisted.of(input.outputPath);
    }
}
