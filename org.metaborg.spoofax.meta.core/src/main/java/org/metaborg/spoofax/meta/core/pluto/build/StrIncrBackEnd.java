package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nullable;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class StrIncrBackEnd extends SpoofaxBuilder<StrIncrBackEnd.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 7275406432476758521L;

        public final Origin frontEndTasks;
        public final @Nullable String strategyName;
        public final Collection<File> strategyContributions;
        public final boolean isBoilerplate;

        public Input(SpoofaxContext context, Origin frontEndTasks, @Nullable String strategyName,
                Collection<File> strategyContributions, boolean isBoilerplate) {
            super(context);

            this.frontEndTasks = frontEndTasks;
            this.strategyName = strategyName;
            this.strategyContributions = strategyContributions;
            this.isBoilerplate = isBoilerplate;
        }
    }

    // Just a type alias
    public static class BuildRequest extends
            build.pluto.builder.BuildRequest<Input, None, StrIncrBackEnd, SpoofaxBuilderFactory<Input, None, StrIncrBackEnd>> {
        private static final long serialVersionUID = -1299552527869341531L;

        public BuildRequest(SpoofaxBuilderFactory<Input, None, StrIncrBackEnd> factory, Input input) {
            super(factory, input);
        }
    }

    public static SpoofaxBuilderFactory<Input, None, StrIncrBackEnd> factory = SpoofaxBuilderFactoryFactory
            .of(StrIncrBackEnd.class, Input.class);

    public static BuildRequest request(Input input) {
        return new BuildRequest(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    public StrIncrBackEnd(Input input) {
        super(input);
    }

    @Override
    protected None build(Input input) throws Throwable {
        // TODO implement
        return None.val;
    }

    @Override
    protected String description(Input input) {
        return "Combine and compile Stratego separate strategy ast files to Java file";
    }

    @Override
    public File persistentPath(Input input) {
        String relname = context.baseDir.toPath().toString().replace(File.separatorChar, '_') + "_";
        if(input.isBoilerplate) {
            relname += "boilerplate";
        } else {
            relname += input.strategyName;
        }
        return context.depPath("str_sep_front." + relname + ".dep");
    }

}
