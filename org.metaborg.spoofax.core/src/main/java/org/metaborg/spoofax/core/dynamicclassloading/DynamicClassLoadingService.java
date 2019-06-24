package org.metaborg.spoofax.core.dynamicclassloading;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class DynamicClassLoadingService implements IDynamicClassLoadingService, ILanguageCache {
    private static final ILogger logger = LoggerUtils.logger(DynamicClassLoadingService.class);

    private final IResourceService resourceService;
    private final Set<ClassLoader> additionalClassLoaders;

    private final Map<ILanguageComponent, ClassLoader> classLoaderCache = Maps.newHashMap();
    private final Map<ILanguageComponent, Map<Class<?>, ServiceLoader<?>>> serviceLoaderCache = Maps.newHashMap();
    private final Injector injector;


    @Inject public DynamicClassLoadingService(IResourceService resourceService,
        Set<ClassLoader> additionalClassLoaders, Injector injector) {
        this.resourceService = resourceService;
        this.additionalClassLoaders = additionalClassLoaders;
        this.injector = injector;
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
        T instance;
        try {
            logger.trace("Instantiating class " + className);
            instance = (T) theClass.newInstance();
            injector.injectMembers(instance);
        } catch (InstantiationException e) {
            throw new MetaborgException("Given class was not instantiable: " + className, e);
        } catch (IllegalAccessException e) {
            throw new MetaborgException("Given class was not accessible: " + className, e);
        } catch (ClassCastException e) {
            throw new MetaborgException("Given class does not implement required interface: " + className, e);
        }
        logger.trace("Successfully loaded and instantiated class " + className);
        return instance;
    }

    @Override
    public <T> List<T> loadClasses(ILanguageComponent component, Class<T> type) throws MetaborgException {
        final Map<Class<?>, ServiceLoader<?>> serviceLoaderCacheLevel2;
        if(serviceLoaderCache.containsKey(component)) {
            serviceLoaderCacheLevel2 = serviceLoaderCache.get(component);
            if(serviceLoaderCacheLevel2.containsKey(type)) {
                @SuppressWarnings("unchecked")
                ServiceLoader<T> serviceLoader = (ServiceLoader<T>) serviceLoaderCacheLevel2.get(type);
                return initializeInjectionFields(serviceLoader.iterator());
            }
        } else {
            serviceLoaderCacheLevel2 = Maps.newHashMap();
            serviceLoaderCache.put(component, serviceLoaderCacheLevel2);
        }
        final ClassLoader classLoader = classLoader(component);
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(type, classLoader);
        serviceLoaderCacheLevel2.put(type, serviceLoader);
        return initializeInjectionFields(serviceLoader.iterator());
    }

    private <T> List<T> initializeInjectionFields(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        for(; iterator.hasNext();) {
            T instance = iterator.next();
            injector.injectMembers(instance);
            result.add(instance);
        }
        return result;
    }

    private ClassLoader classLoader(ILanguageComponent component) throws MetaborgException {
        if(classLoaderCache.containsKey(component)) {
            return classLoaderCache.get(component);
        }
        final Collection<FileObject> jarFiles = component.facet(DynamicClassLoadingFacet.class).jarFiles;
        final URL[] classpath = new URL[jarFiles.size()];
        try {
            int i = 0;
            for(FileObject jar : jarFiles) {
                final File localJar = resourceService.localFile(jar);
                classpath[i] = localJar.toURI().toURL();
                i++;
            }
        } catch (MalformedURLException e) {
            throw new MetaborgException(e);
        }
        logger.trace("Loading jar files {}", (Object) classpath);
        URLClassLoader classLoader = new URLClassLoader(classpath, new DynamicClassLoader(additionalClassLoaders));
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
