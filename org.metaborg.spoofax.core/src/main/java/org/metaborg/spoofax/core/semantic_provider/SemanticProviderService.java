package org.metaborg.spoofax.core.semantic_provider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.user_definable.IHoverText;
import org.metaborg.spoofax.core.user_definable.IOutliner;
import org.metaborg.spoofax.core.user_definable.IResolver;
import org.metaborg.spoofax.core.user_definable.ITransformer;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class SemanticProviderService implements ISemanticProviderService, ILanguageCache {
    private static final ILogger logger = LoggerUtils.logger(SemanticProviderService.class);

    private final IResourceService resourceService;
    private final Set<ClassLoader> additionalClassLoaders;

    private final Map<ILanguageComponent, ClassLoader> classLoaderCache = Maps.newHashMap();
    @SuppressWarnings("rawtypes")
    private final Map<ILanguageComponent, Map<Class, ServiceLoader>> serviceLoaderCache = Maps.newHashMap();


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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadClass(ILanguageComponent component, String className, Class<T> expectedType) throws MetaborgException {
        final ClassLoader classLoader = classLoader(component);
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

    @Override
    public <T> Iterator<T> loadClasses(ILanguageComponent component, Class<T> type) throws MetaborgException {
        @SuppressWarnings("rawtypes")
        final Map<Class, ServiceLoader> serviceLoaderCacheLevel2;
        if(serviceLoaderCache.containsKey(component)) {
            serviceLoaderCacheLevel2 = serviceLoaderCache.get(component);
            if(serviceLoaderCacheLevel2.containsKey(type)) {
                @SuppressWarnings("unchecked")
                ServiceLoader<T> serviceLoader = (ServiceLoader<T>) serviceLoaderCacheLevel2.get(type);
                return serviceLoader.iterator();
            }
        } else {
            serviceLoaderCacheLevel2 = Maps.newHashMap();
            serviceLoaderCache.put(component, serviceLoaderCacheLevel2);
        }
        final ClassLoader classLoader = classLoader(component);
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(type, classLoader);
        serviceLoaderCacheLevel2.put(type, serviceLoader);
        return serviceLoader.iterator();
    }

    private ClassLoader classLoader(ILanguageComponent component) throws MetaborgException {
        if(classLoaderCache.containsKey(component)) {
            return classLoaderCache.get(component);
        }
        Iterable<FileObject> jarFiles = component.facet(SemanticProviderFacet.class).jarFiles;
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
        URLClassLoader classLoader = new URLClassLoader(classpath, new SemanticProviderClassLoader(additionalClassLoaders));
        classLoaderCache.put(component, classLoader);
        return classLoader;
    }

    @Override
    public void invalidateCache(ILanguageComponent component) {
        serviceLoaderCache.remove(component);
        classLoaderCache.remove(component);
    }

    @Override
    public void invalidateCache(ILanguageImpl impl) {
        for (ILanguageComponent component : impl.components()) {
            invalidateCache(component);
        }
    }
}
