package org.strategoxt.imp.metatooling;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LoaderPreferences {
	private final static String PREFERENCES_NODE = "org.strategoxt.imp.metatooling";
	
	private final static String DESCRIPTORS_KEY = "editors";
	
	private final static String SEPARATOR = "\0";

	private final IEclipsePreferences preferences;
	
	private LoaderPreferences(IProject project) {
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
	
	public void putDescriptor(String descriptor) {
		if (!hasDescriptor(descriptor))
			preferences.put(DESCRIPTORS_KEY, preferences.get(DESCRIPTORS_KEY, "") + ",");
	}
	
	public void removeDescriptor(String descriptor) {
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
}
