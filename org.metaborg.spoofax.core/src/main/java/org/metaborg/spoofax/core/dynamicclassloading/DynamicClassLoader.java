package org.metaborg.spoofax.core.dynamicclassloading;

import org.strategoxt.HybridInterpreter;

public class DynamicClassLoader extends ClassLoader {
    private final ClassLoader strategoClassLoader = HybridInterpreter.class.getClassLoader();
    private final Iterable<ClassLoader> additionalClassLoaders;


    public DynamicClassLoader(Iterable<ClassLoader> additionalClassLoaders) {
        super(DynamicClassLoader.class.getClassLoader());
        this.additionalClassLoaders = additionalClassLoaders;
    }


    @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch(ClassNotFoundException e) {
        }

        try {
            return strategoClassLoader.loadClass(name);
        } catch(ClassNotFoundException e) {
        }

        for(ClassLoader classLoader : additionalClassLoaders) {
            try {
                return classLoader.loadClass(name);
            } catch(ClassNotFoundException e) {
            }
        }

        throw new ClassNotFoundException("Class " + name + " could not be resolved inside a Stratego runtime");
    }
}
