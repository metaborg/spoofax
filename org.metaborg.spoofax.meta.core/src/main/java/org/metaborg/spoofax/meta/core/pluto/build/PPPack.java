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
import org.strategoxt.tools.main_parse_pp_table_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class PPPack extends SpoofaxBuilder<PPPack.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -5786344696509159033L;

        public final File ppInput;
        public final File ppTermOutput;

        public final String sdfModule;
        public final String sdfArgs;

        public Input(SpoofaxContext context, File ppInput, File ppTermOutput, String sdfModule, String sdfArgs) {
            super(context);
            this.ppInput = ppInput;
            this.ppTermOutput = ppTermOutput;
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, PPPack> factory = SpoofaxBuilderFactoryFactory.of(PPPack.class,
        Input.class);


    public PPPack(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, PPPack, SpoofaxBuilderFactory<Input, None, PPPack>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compress pretty-print table";
    }

    @Override public File persistentPath(Input input) {
        Path rel = FileCommands.getRelativePath(context.baseDir, input.ppTermOutput);
        String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("pp-pack." + relname + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        if(!context.isBuildStrategoEnabled(this)) {
            return None.val;
        }

        requireBuild(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, input.sdfArgs));

        require(input.ppInput);
        if(!FileCommands.exists(input.ppInput)) {
            FileCommands.writeToFile(input.ppTermOutput, "PP-Table([])");
            provide(input.ppTermOutput);
        } else {
            ExecutionResult result =
                StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), main_parse_pp_table_0_0.instance,
                    "parse-pp-table", newResourceTracker(), "-i", input.ppInput, "-o", input.ppTermOutput);
            provide(input.ppTermOutput);
            setState(State.finished(result.success));
        }

        return None.val;
    }
}
