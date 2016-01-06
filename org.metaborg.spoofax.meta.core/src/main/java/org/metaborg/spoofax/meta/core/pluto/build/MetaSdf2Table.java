package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -3179663405417276186L;

        public final String metaSdfModule;
        public final Arguments sdfArgs;


        public Input(SpoofaxContext context, String metaModule, Arguments sdfArgs) {
            super(context);
            this.metaSdfModule = metaModule;
            this.sdfArgs = sdfArgs;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, MetaSdf2Table> factory = SpoofaxBuilderFactoryFactory.of(
        MetaSdf2Table.class, Input.class);


    public MetaSdf2Table(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, MetaSdf2Table, SpoofaxBuilderFactory<Input, None, MetaSdf2Table>> request(
        Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Compile metagrammar for concrete object syntax";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("meta-sdf2table." + input.metaSdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        final File metaModule = toFile(context.settings.getSdfMainFile(input.metaSdfModule));
        require(metaModule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);

        final boolean available = FileCommands.exists(metaModule);
        if(available) {
            final FileObject strategoMixPath = context.resourceService().resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = toFileReplicate(strategoMixPath);
            provide(strategoMixFile);


            final Arguments sdfArgs = new Arguments().addFile("-Idef", strategoMixFile).addAll(input.sdfArgs);
            requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metaSdfModule, sdfArgs));
        }

        return None.val;
    }
}
