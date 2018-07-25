package org.metaborg.spoofax.core.semantic_provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.user_definable.IHoverText;
import org.metaborg.spoofax.core.user_definable.IOutliner;
import org.metaborg.spoofax.core.user_definable.IResolver;
import org.metaborg.spoofax.core.user_definable.ITransformer;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

public class SemanticProviderService implements ISemanticProviderService {
    private static final ILogger logger = LoggerUtils.logger(SemanticProviderService.class);

    private final IResourceService resourceService;
    private final Set<ClassLoader> additionalClassLoaders;


    @Inject public SemanticProviderService(IResourceService resourceService,
        Set<ClassLoader> additionalClassLoaders) {
        this.resourceService = resourceService;
        this.additionalClassLoaders = additionalClassLoaders;
    }

    @Override
    public IOutliner outliner(ILanguageComponent component, String className) throws MetaborgException {
        return loadClass(component, className, IOutliner.class);
    }

    @Override
    public IResolver resolver(ILanguageComponent component, String className) throws MetaborgException {
        return loadClass(component, className, IResolver.class);
    }

    @Override
    public IHoverText hoverer(ILanguageComponent component, String className) throws MetaborgException {
        return loadClass(component, className, IHoverText.class);
    }

    @Override
    public ITransformer transformer(ILanguageComponent component, String className) throws MetaborgException {
        return loadClass(component, className, ITransformer.class);
    }

    @Override
    public <T> T loadClass(ILanguageComponent component, String className, Class<T> expectedType) throws MetaborgException {
        return loadClass(component.facet(SemanticProviderFacet.class), className, expectedType);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadClass(SemanticProviderFacet facet, String className, Class<T> expectedType) throws MetaborgException {
        Iterable<FileObject> jarFiles = facet.jarFiles;
        final ClassLoader classLoader = classLoader(jarFiles);
        Class<?> theClass;
        try {
            logger.trace("Loading outliner class");
            theClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MetaborgException("Given class was not found: " + className, e);
        }
        T outliner;
        try {
            logger.trace("Instantiating outliner class");
            outliner = (T) theClass.newInstance();
        } catch (InstantiationException e) {
            throw new MetaborgException("Given class was not instantiable", e);
        } catch (IllegalAccessException e) {
            throw new MetaborgException("Given class was not accessible", e);
        } catch (ClassCastException e) {
            throw new MetaborgException("Given class does not implement required interface", e);
        }
        return outliner;
    }

    private ClassLoader classLoader(Iterable<FileObject> jarFiles) throws MetaborgException {
        URL[] classpath;
        try {
            classpath = StreamSupport.stream(jarFiles.spliterator(), false).map(jar -> {
                final File localJar = resourceService.localFile(jar);
                try {
                    return localJar.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new MetaborgRuntimeException(e);
                }
            }).toArray(i -> new URL[i]);
        } catch (MetaborgRuntimeException e) {
            throw new MetaborgException(e);
        }
        logger.trace("Loading jar files {}", (Object) classpath);
        return new URLClassLoader(classpath, new SemanticProviderClassLoader(additionalClassLoaders));
    }
}
