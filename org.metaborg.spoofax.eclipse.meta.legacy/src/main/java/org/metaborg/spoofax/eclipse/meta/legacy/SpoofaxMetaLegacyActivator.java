package org.metaborg.spoofax.eclipse.meta.legacy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SpoofaxMetaLegacyActivator implements BundleActivator {
    private static BundleContext context;


    @Override public void start(BundleContext bundleContext) throws Exception {
        context = bundleContext;
    }

    @Override public void stop(BundleContext bundleContext) throws Exception {
        context = null;
    }


    public static BundleContext context() {
        return context;
    }
}
