/**
 * 
 */
package org.strategoxt.imp.metatooling.loading;

import static org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader.getSourceDescriptor;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.strategoxt.imp.metatooling.building.DynamicDescriptorBuilder;
import org.strategoxt.imp.metatooling.utils.ResourceUtil;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author vladvergu
 * 
 */
public class AntDescriptorLoader {

	/**
	 * (Re)load a descriptor
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Descriptor file expected");

		Environment.getStrategoLock().lock();

		try {
			final String descriptor = args[0];
			final IResource source = ResourceUtil.getResource(getSourceDescriptor(descriptor));
			final DynamicDescriptorBuilder builder = DynamicDescriptorBuilder.getInstance();

			if (!source.exists()) {
				Environment.logException("Could not find source descriptor:" + source, new FileNotFoundException(source
						.getFullPath().toOSString()));
				System.err.println("Build failed: could not find source descriptor " + source);
				System.exit(1);
			}

			DynamicDescriptorLoader.getInstance().loadPackedDescriptor(builder.getTargetDescriptor(source));
		} finally {
			Environment.getStrategoLock().unlock();
		}
	}

}
