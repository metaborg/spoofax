package org.metaborg.spoofax.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageFacetFactory;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.LanguageService;
import org.metaborg.spoofax.core.parser.IParseService;
import org.metaborg.spoofax.core.parser.jsglr.JSGLRParseService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.TermFactoryService;
import org.metaborg.spoofax.eclipse.ant.AntDescriptorBuilder;
import org.metaborg.spoofax.eclipse.ant.AntDescriptorLoader;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;
import org.strategoxt.imp.metatooling.wizards.NewEditorWizard;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorRegistry;
import org.strategoxt.imp.runtime.dynamicloading.DynamicDescriptorBuilder;
import org.strategoxt.imp.runtime.dynamicloading.DynamicDescriptorLoader;
import org.strategoxt.imp.runtime.dynamicloading.StartupDescriptorLoader;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class SpoofaxEclipseModule extends AbstractModule {
    private final ClassLoader resourceClassLoader;

    public SpoofaxEclipseModule(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }

    public SpoofaxEclipseModule() {
        this(SpoofaxEclipseModule.class.getClassLoader());
    }

    @Override protected void configure() {
        try {
            bind(IResourceService.class).to(ResourceService.class).in(Singleton.class);
            bind(ITermFactoryService.class).to(TermFactoryService.class).in(Singleton.class);
            bind(ILanguageService.class).to(LanguageService.class).in(Singleton.class);
            bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);
            bind(IParseService.class).to(JSGLRParseService.class).in(Singleton.class);

            bind(DescriptorRegistry.class).in(Singleton.class);
            bind(DescriptorFactory.class).asEagerSingleton();
            bind(DynamicDescriptorLoader.class).in(Singleton.class);
            bind(DynamicDescriptorBuilder.class).in(Singleton.class);
            bind(StartupDescriptorLoader.class).in(Singleton.class);

            bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(Singleton.class);

            @SuppressWarnings("unused") final Multibinder<ILanguageFacetFactory> facetFactoriesBinder =
                Multibinder.newSetBinder(binder(), ILanguageFacetFactory.class);

            bind(ClassLoader.class).annotatedWith(Names.named("ResourceClassLoader")).toInstance(resourceClassLoader);

            requestStaticInjection(AntDescriptorBuilder.class);
            requestStaticInjection(AntDescriptorLoader.class);
            requestStaticInjection(NewEditorWizard.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
