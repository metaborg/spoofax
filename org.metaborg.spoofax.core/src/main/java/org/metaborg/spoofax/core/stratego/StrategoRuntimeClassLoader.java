package org.metaborg.spoofax.core.stratego;

import org.metaborg.meta.lang.dynsem.interpreter.DynSemContext;
import org.strategoxt.HybridInterpreter;

import com.github.krukow.clj_ds.PersistentMap;

public class StrategoRuntimeClassLoader extends ClassLoader {
    private final ClassLoader strategoClassLoader = HybridInterpreter.class.getClassLoader();
    private final ClassLoader cjdsClassLoader = PersistentMap.class.getClassLoader();
    private final ClassLoader dynsemClassLoader = DynSemContext.class.getClassLoader();


    public StrategoRuntimeClassLoader() {
        super(StrategoRuntimeClassLoader.class.getClassLoader());
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

		try {
			return cjdsClassLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
		}

		return dynsemClassLoader.loadClass(name);
	}
}
