package org.metaborg.spoofax.eclipse.meta;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Injector;

public class SpoofaxMetaPlugin extends AbstractUIPlugin {
    public static final String id = "org.metaborg.spoofax.eclipse.meta";

    private static SpoofaxMetaPlugin plugin;
    private static BundleContext bundleContext;


    @Override public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;
    }

    @Override public void stop(BundleContext context) throws Exception {
        plugin = null;
        bundleContext = null;
        super.stop(context);
    }


    public static SpoofaxMetaPlugin plugin() {
        return plugin;
    }

    public static BundleContext context() {
        return bundleContext;
    }

    public static Injector injector() {
        return SpoofaxPlugin.injector();
    }
}
