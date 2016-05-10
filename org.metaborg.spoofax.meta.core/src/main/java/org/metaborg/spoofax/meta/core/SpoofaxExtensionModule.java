package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.meta.core.stratego.primitives.LanguageSpecNamePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitives.PlaceholderCharsPrimitive;
import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Module for extending {@link Spoofax}.
 * 
 * Note that this module is loaded together with a {@link SpoofaxModule}, so only those bindings are available. Bindings
 * from {@link SpoofaxMetaModule} are NOT available. To inject bindings from {@link SpoofaxMetaModule}, use static
 * injection and request injection in the {@link SpoofaxMetaModule}.
 */
public class SpoofaxExtensionModule extends AbstractModule {
    @Override protected void configure() {
        // Extend Spoofax's primitive library.
        final Multibinder<AbstractPrimitive> spoofaxPrimitiveLibrary =
            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named("SpoofaxPrimitiveLibrary"));
        spoofaxPrimitiveLibrary.addBinding().to(LanguageSpecNamePrimitive.class).in(Singleton.class);
        spoofaxPrimitiveLibrary.addBinding().to(PlaceholderCharsPrimitive.class).in(Singleton.class);
    }
}
