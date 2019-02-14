package org.metaborg.spoofax.meta.core.pluto.util;

import java.io.Serializable;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.IgnoreOutputStamper;
import build.pluto.output.Output;

/**
 * Origin builder that overrides stampers of all added build requests with {@link IgnoreOutputStamper}.
 */
public class OutputIgnoreOriginBuilder extends Origin.Builder {
    public OutputIgnoreOriginBuilder() {
        super();
    }

    @Override public Origin.Builder add(BuildRequest<?, ?, ?, ?>... requests) {
        for(BuildRequest<?, ?, ?, ?> buildRequest : requests) {
            @SuppressWarnings("unchecked") BuilderFactory<Serializable, Output, Builder<Serializable, Output>> factory =
                (BuilderFactory<Serializable, Output, Builder<Serializable, Output>>) buildRequest.factory;
            super.add(
                new BuildRequest<Serializable, Output, Builder<Serializable, Output>, BuilderFactory<Serializable, Output, Builder<Serializable, Output>>>(
                    factory, buildRequest.input, IgnoreOutputStamper.instance));
        }
        return this;
    }

    @Override public Origin.Builder add(Origin origin) {
        return add(origin.getReqs().toArray(new BuildRequest<?, ?, ?, ?>[0]));
    }

    /**
     * Gives back a new origin where all build requests have their stamper overridden with {@link IgnoreOutputStamper}.
     */
    public static Origin ignoreOutputs(Origin origin) {
        return new OutputIgnoreOriginBuilder().add(origin).get();
    }
}
