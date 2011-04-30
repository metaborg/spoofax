package org.strategoxt.imp.metatooling.building;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.strategoxt.imp.metatooling.loading.DynamicDescriptorLoader;
import org.strategoxt.imp.runtime.Environment;

/**
 * Triggers descriptor building and loading from an Ant build file.
 */
public class AntSpxDescriptorBuilder {

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
				String descriptor = args[0]; // esv-main.packed.esv file name is specified 

				IResource source = getResource(descriptor);

				if (!source.exists()) {
					Environment.logException("Could not find descriptor:" + source, new FileNotFoundException(source.getFullPath().toOSString()));
					System.err.println("Build failed: could not find descriptor " + source);
					System.exit(1);
				}
				
				boolean success = loadEditorService(source , new NullProgressMonitor());

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

	private static IResource getResource(String file) {
		File fileRef = new File(file);
		try {
			fileRef = fileRef.getCanonicalFile();
		} catch (IOException e) {
			Environment.logException(e);
		}
		URI uri = fileRef.toURI();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource[] resources = workspace.getRoot().findFilesForLocationURI(uri);
		if (resources.length == 0)
			throw new IllegalArgumentException("File not in workspace: " + file);

		IResource resource = resources[0];
		return resource;
	}

	
	private static boolean loadEditorService(IResource descriptor, IProgressMonitor monitor) {
		IPath location = descriptor.getRawLocation();
		if (location == null) return false;
	
		try {
			monitor.setTaskName("Loading " + descriptor.getName());
			
			if (descriptor.exists()) {
				Environment.assertLock();
				monitor.setTaskName("Loading " + descriptor.getName());
				if (AntSpxDescriptorBuilder.isActive())
					System.err.println("Loading new editor services");

				DynamicDescriptorLoader.getInstance().loadPackedDescriptor(descriptor);

				monitor.setTaskName(null);
			}
			return true;

		} catch (Exception e) {
			Environment.logException("Unable to load new editor services for " + descriptor, e);
			return false;
		}
	}

}
