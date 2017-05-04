package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.stratego.primitive.SpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacySpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.meta.core.stratego.primitive.CheckSdf2TablePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.GetContextualGrammarPrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LanguageSpecPpNamePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LanguageSpecificationPrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.LegacyLanguageSpecNamePrimitive;
import org.metaborg.spoofax.meta.core.stratego.primitive.PlaceholderCharsPrimitive;
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

            Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(SpoofaxPrimitiveLibrary.name));
        spoofaxPrimitiveLibrary.addBinding().to(LanguageSpecificationPrimitive.class).in(Singleton.class);
        spoofaxPrimitiveLibrary.addBinding().to(CheckSdf2TablePrimitive.class).in(Singleton.class);
        spoofaxPrimitiveLibrary.addBinding().to(PlaceholderCharsPrimitive.class).in(Singleton.class);
        spoofaxPrimitiveLibrary.addBinding().to(LanguageSpecPpNamePrimitive.class).in(Singleton.class);
        spoofaxPrimitiveLibrary.addBinding().to(GetContextualGrammarPrimitive.class).in(Singleton.class);
        
        final Multibinder<AbstractPrimitive> legacySpoofaxPrimitiveLibrary = Multibinder.newSetBinder(binder(),
            AbstractPrimitive.class, Names.named(LegacySpoofaxPrimitiveLibrary.name));
        legacySpoofaxPrimitiveLibrary.addBinding().to(LegacyLanguageSpecNamePrimitive.class).in(Singleton.class);

    }
}
