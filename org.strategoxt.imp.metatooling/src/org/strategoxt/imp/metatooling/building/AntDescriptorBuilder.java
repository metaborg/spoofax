package org.strategoxt.imp.metatooling.building;

import static org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader.getSourceDescriptor;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.metatooling.utils.ResourceUtil;
import org.strategoxt.imp.runtime.Environment;

/**
 * Triggers descriptor building and loading from an Ant build file.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AntDescriptorBuilder {
	
	private static volatile boolean active;
	
	public static void main(String[] args) {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Descriptor file expected");
		
		DynamicDescriptorBuilder builder = DynamicDescriptorBuilder.getInstance();
		if (builder.isAntBuildDisallowed())
			throw new IllegalStateException("Cannot load new editor at this time: try again after background loading is completed");
		
		Environment.getStrategoLock().lock();
		try {
			active = true;
			try {
				String descriptor = args[0];
				
				IResource source = ResourceUtil.getResource(getSourceDescriptor(descriptor));
				if (!source.exists()) {
					Environment.logException("Could not find source descriptor:" + source, new FileNotFoundException(source.getFullPath().toOSString()));
					System.err.println("Build failed: could not find source descriptor " + source);
					System.exit(1);
				}
				boolean success = builder.updateResource(source, new NullProgressMonitor());
				if (!success) {
					System.err.println("Build failed; see error log.");
					System.exit(1);
				}
			} finally {
				active = false;
			}
		} finally {
			Environment.getStrategoLock().unlock();
		}
	}
	
	public static boolean isActive() {
		return active;
	}

	
}
