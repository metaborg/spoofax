package org.metaborg.spoofax.core.stratego;

import org.strategoxt.HybridInterpreter;

public class StrategoRuntimeClassLoader extends ClassLoader {
    private final ClassLoader strategoClassLoader = HybridInterpreter.class.getClassLoader();
    private final Iterable<ClassLoader> additionalClassLoaders;


    public StrategoRuntimeClassLoader(Iterable<ClassLoader> additionalClassLoaders) {
        super(StrategoRuntimeClassLoader.class.getClassLoader());
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
