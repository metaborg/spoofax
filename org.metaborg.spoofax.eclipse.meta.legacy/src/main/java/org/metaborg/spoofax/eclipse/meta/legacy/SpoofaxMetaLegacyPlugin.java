package org.metaborg.spoofax.eclipse.meta.legacy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.strategoxt.imp.metatooling.stratego.MetaSPILibrary;

public class SpoofaxMetaLegacyPlugin implements BundleActivator {
    public static final String id = "org.metaborg.spoofax.eclipse.meta.legacy";

    private static SpoofaxMetaLegacyPlugin plugin;
    private static BundleContext bundleContext;


    @Override public void start(BundleContext context) throws Exception {
        plugin = this;
        bundleContext = context;
        
        MetaSPILibrary.init();
    }

    @Override public void stop(BundleContext context) throws Exception {
        context = null;
        plugin = null;
    }


    public static SpoofaxMetaLegacyPlugin plugin() {
        return plugin;
    }

    public static BundleContext context() {
        return bundleContext;
    }
}
