package org.strategoxt.imp.metatooling.building;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorUpdater;
import org.strategoxt.imp.runtime.Environment;

/**
 * Triggers descriptor building and loading from an Ant build file.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AntDescriptorBuilder {
	public static void main(String[] args) {
		if (args == null || args.length == 0 || !new File(args[0]).exists())
			throw new IllegalArgumentException("Existing descriptor file expected: " + Arrays.toString(args));
		
		URI uri = new File(args[0]).toURI();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource[] resources = workspace.getRoot().findFilesForLocationURI(uri);
		if (resources.length == 0)
			throw new IllegalArgumentException("File not in workspace: " + args[0]);
		
		synchronized (Environment.getSyncRoot()) {
			DynamicDescriptorUpdater.getInstance().updateResource(resources[0], new NullProgressMonitor(), false);
		}
	}
}
