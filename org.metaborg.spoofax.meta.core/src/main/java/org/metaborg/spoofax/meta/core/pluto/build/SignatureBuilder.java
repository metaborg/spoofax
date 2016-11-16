package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.meta.core.signature.ISig;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;

public class SignatureBuilder extends SpoofaxBuilder<SignatureBuilder.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 815468752440037271L;

        public final Origin origin;


        public Input(SpoofaxContext context, @Nullable Origin origin) {
            super(context);
            this.origin = origin;
        }
    }


    public static final SpoofaxBuilderFactory<Input, None, SignatureBuilder> factory =
        SpoofaxBuilderFactoryFactory.of(SignatureBuilder.class, Input.class);


    public SignatureBuilder(Input input) {
        super(input);
    }

    public static BuildRequest<Input, None, SignatureBuilder, SpoofaxBuilderFactory<Input, None, SignatureBuilder>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Extract signatures";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("signature.dep");
    }

    @Override protected None build(Input input) throws Throwable {
        requireBuild(input.origin);

        final ILanguageSpec languageSpec = context.languageSpec;
        final FileObject root = languageSpec.location();
        final Iterable<ISig> sigs = context.sigService().extract(languageSpec, this);
        context.sigSerializer().write(root, sigs, this);

        return None.val;
    }
}
