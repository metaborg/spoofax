package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.stratego.primitives.LanguageSpecNamePrimitive;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Module for extending {@link Spoofax}
 */
public class SpoofaxExtensionModule extends AbstractModule {
    @Override protected void configure() {
        // Extend Spoofax primitive library.
        final Multibinder<AbstractPrimitive> spoofaxPrimitiveLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named("SpoofaxPrimitiveLibrary"));
        spoofaxPrimitiveLibrary.addBinding().to(LanguageSpecNamePrimitive.class).in(Singleton.class);
    }
}
