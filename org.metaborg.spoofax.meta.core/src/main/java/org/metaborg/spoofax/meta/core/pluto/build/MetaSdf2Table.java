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
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.base.Joiner;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -3179663405417276186L;

        public final String metaModule;


        public Input(SpoofaxContext context, String metaModule) {
            super(context);
            this.metaModule = metaModule;
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
        return context.depPath("meta-sdf2table." + input.metaModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        final File metaModule = FileUtils.toFile(context.settings.getSdfMainFile(input.metaModule));
        require(metaModule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);

        final boolean available = FileCommands.exists(metaModule);
        if(available) {
            final FileObject strategoMixPath = context.resourceService.resolve(NativeBundle.getStrategoMix());
            final File strategoMixFile = context.resourceService.localFile(strategoMixPath);
            provide(strategoMixFile);

            final String sdfArgs = "-Idef " + strategoMixFile + " " + Joiner.on(' ').join(context.settings.sdfArgs());
            requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metaModule, sdfArgs));
        }

        return None.val;
    }
}
