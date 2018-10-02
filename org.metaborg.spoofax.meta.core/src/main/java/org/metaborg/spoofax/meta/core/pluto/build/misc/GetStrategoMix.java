package org.metaborg.spoofax.meta.core.pluto.build.misc;

import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.pluto.*;
import org.metaborg.spoofax.nativebundle.NativeBundle;

public class GetStrategoMix extends SpoofaxBuilder<GetStrategoMix.Input, OutputPersisted<File>> {
    /**
     * Empty input class to ensure that a new input instance is always created, which is always exactly the same, such
     * that this builder is only executed once.
     */
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6282893509044981524L;

        public Input(SpoofaxContext context) {
            super(context);
        }
    }

    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, GetStrategoMix> factory =
        SpoofaxBuilderFactoryFactory.of(GetStrategoMix.class, Input.class);


    public GetStrategoMix(Input input) {
        super(input);
    }


    public static
        BuildRequest<Input, OutputPersisted<File>, GetStrategoMix, SpoofaxBuilderFactory<Input, OutputPersisted<File>, GetStrategoMix>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }


    @Override protected String description(Input input) {
        return "Get Stratego mix file";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("get-stratego-mix.dep");
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        final URI strategoMixUri = NativeBundle.getStrategoMix();
        final FileObject strategoMixLocation = context.resourceService().resolve(strategoMixUri);
        final File strategoMixFile = toFileReplicate(strategoMixLocation);
        provide(strategoMixFile, LastModifiedStamper.instance);
        return OutputPersisted.of(strategoMixFile);
    }
}
