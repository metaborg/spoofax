package org.strategoxt.imp.metatooling;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LoaderPreferences {
	private final static String PREFERENCES_NODE = "org.strategoxt.imp.metatooling";
	
	private final static String DESCRIPTORS_KEY = "editors";
	
	private final static String SEPARATOR = "\0";
	
	private final static Timer delayedRun = new Timer();

	private final IProject project;
	
	private final IEclipsePreferences preferences;
	
	private LoaderPreferences(IProject project) {
		this.project = project;
		IScopeContext projectScope = new ProjectScope(project);
		preferences = projectScope.getNode(PREFERENCES_NODE);
	}
	
	public static LoaderPreferences get(IProject project) {
		return new LoaderPreferences(project);
	}
	
	public String[] getDescriptors() {
		String descriptors = preferences.get(DESCRIPTORS_KEY, "");
		
		if (descriptors.equals("")) {
			return new String[0];
		} else {
			return descriptors.split(SEPARATOR);
		}
	}
	
	public boolean hasDescriptor(String descriptor) {
		String[] descriptors = getDescriptors();
		
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptor.equals(descriptors[i]))
				return true;
		}
		
		return false;
	}
	
	public void putDescriptor(final String descriptor) {
		setDescriptors(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					if (!hasDescriptor(descriptor)) {
						String value = preferences.get(DESCRIPTORS_KEY, "")
								+ SEPARATOR + descriptor;
						preferences.put(DESCRIPTORS_KEY, value);
						preferences.sync();
					}
				} catch (BackingStoreException e) {
					Environment.logException(
							"Could not store descriptors in project metadata",
							e);
				}
			}
		});
	}
	
	public void removeDescriptor(final String descriptor) {
		setDescriptors(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				List<String> descriptors = Arrays.asList(getDescriptors());
				descriptors.remove(descriptor);
				
				StringBuilder result = new StringBuilder();
				if (descriptors.size() > 0) {
					descriptors.add(descriptors.get(0));
					for (int i = 1; i < descriptors.size(); i++) {
						result.append(SEPARATOR);
						result.append(descriptors.get(i));
					}
				}
				
				preferences.put(DESCRIPTORS_KEY, result.toString());
			}
		});
	}
	
	private void setDescriptors(final IWorkspaceRunnable job) {
		// HACK: Workspace is locked, try again later
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (!project.isOpen()) return;
				
				try {
					ResourcesPlugin.getWorkspace().run(job, null);
				} catch (CoreException e) {
					setDescriptors(job);
				}
			}
		};
		
		delayedRun.schedule(task, 100);
	}

}
