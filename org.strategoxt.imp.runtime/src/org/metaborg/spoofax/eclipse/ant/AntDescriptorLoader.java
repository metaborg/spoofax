package org.metaborg.spoofax.eclipse.ant;

import static org.strategoxt.imp.runtime.dynamicloading.DynamicDescriptorLoader.getSourceDescriptor;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.strategoxt.imp.metatooling.utils.ResourceUtil;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicDescriptorBuilder;
import org.strategoxt.imp.runtime.dynamicloading.DynamicDescriptorLoader;

import com.google.inject.Inject;

/**
 * (Re)loads a descriptor
 */
public class AntDescriptorLoader {
    @Inject static private DynamicDescriptorBuilder builder;
    @Inject static private DynamicDescriptorLoader loader;

    public static void main(String[] args) {
        if(args == null || args.length == 0)
            throw new IllegalArgumentException("Descriptor file expected");

        Environment.getStrategoLock().lock();

        try {
            final String descriptor = args[0];
            final IResource source = ResourceUtil.getResource(getSourceDescriptor(descriptor));

            if(!source.exists()) {
                Environment.logException("Could not find source descriptor:" + source, new FileNotFoundException(source
                    .getFullPath().toOSString()));
                System.err.println("Build failed: could not find source descriptor " + source);
                System.exit(1);
            }

            loader.loadPackedDescriptor(builder.getTargetDescriptor(source));
        } finally {
            Environment.getStrategoLock().unlock();
        }
    }
}
