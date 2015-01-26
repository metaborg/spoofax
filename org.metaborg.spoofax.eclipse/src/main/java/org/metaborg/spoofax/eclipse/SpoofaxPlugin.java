package org.metaborg.spoofax.eclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.metaborg.spoofax.eclipse.processing.Processor;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.metaborg.spoofax.eclipse";

    private static SpoofaxPlugin plugin;
    private static Injector injector;


    public SpoofaxPlugin() {

    }


    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        injector = Guice.createInjector(new SpoofaxEclipseModule());

        injector.getInstance(Processor.class).startup();
    }

    public void stop(BundleContext context) throws Exception {
        injector = null;
        plugin = null;
        super.stop(context);
    }

    public static SpoofaxPlugin plugin() {
        return plugin;
    }

    public static Injector injector() {
        return injector;
    }

    public static ImageDescriptor imageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
