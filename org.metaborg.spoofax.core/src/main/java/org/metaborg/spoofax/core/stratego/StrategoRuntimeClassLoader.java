package org.metaborg.spoofax.core.stratego;

import org.strategoxt.HybridInterpreter;

public class StrategoRuntimeClassLoader extends ClassLoader {
    private final ClassLoader strategoClassLoader = HybridInterpreter.class.getClassLoader();


    public StrategoRuntimeClassLoader() {
        super(StrategoRuntimeClassLoader.class.getClassLoader());
    }


    @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch(ClassNotFoundException e) {
            return strategoClassLoader.loadClass(name);
        }
    }
}
