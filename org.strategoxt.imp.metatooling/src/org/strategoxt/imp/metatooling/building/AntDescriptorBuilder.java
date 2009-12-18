package org.strategoxt.imp.metatooling.building;

import static org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader.*;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.runtime.Environment;

/**
 * Triggers descriptor building and loading from an Ant build file.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AntDescriptorBuilder {
	public static void main(String[] args) {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Descriptor file expected");
		
		synchronized (Environment.getSyncRoot()) {
			String descriptor = args[0];
			
			IResource source = getResource(getSourceDescriptor(descriptor));
			DynamicDescriptorBuilder.getInstance().updateResource(source, new NullProgressMonitor());
			
			/* loading is already performed by builder
			System.out.println("Loading " + descriptor);
			IResource target = getResource(descriptor);
			DynamicDescriptorLoader.getInstance().loadPackedDescriptor(target);
			*/
		}
	}

	private static IResource getResource(String file) {
		URI uri = new File(file).toURI();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource[] resources = workspace.getRoot().findFilesForLocationURI(uri);
		if (resources.length == 0)
			throw new IllegalArgumentException("File not in workspace: " + file);

		IResource resource = resources[0];
		return resource;
	}
}
