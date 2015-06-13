package org.metaborg.spoofax.meta.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class SpoofaxMetaModule extends AbstractModule {
    @Override protected void configure() {
        bind(SpoofaxMetaBuilder.class).in(Singleton.class);
    }
}
