package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.library.index.notification.INotificationService;
import org.spoofax.interpreter.library.index.notification.NotificationCenter;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;

/**
 * A {@link INotificationService} that uses {@link StrategoAnalysisQueueFactory} .
 * 
 * Receives notifications sent by {@link FileNotificationServer} to the {@link NotificationCenter}.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class QueueAnalysisService implements INotificationService {

	public void notifyChanges(URI file, boolean triggerOnSave) {
		try {
			final IProject project = EditorIOAgent.getProject(new File(file));
			final IPath relPath = relativePath(file);

			StrategoAnalysisQueueFactory.getInstance().queueAnalysis(relPath, project, triggerOnSave);
		} catch(FileNotFoundException e) {
			Environment.logException("Background language service failed", e);
		} catch(RuntimeException e) {
			Environment.logException("Background language service failed", e);
		}
	}

	public void notifyChanges(Iterable<URI> files, boolean triggerOnSave) {
		if(!files.iterator().hasNext())
			return;

		final Map<Entry<IProject, Language>, List<IPath>> projects = new HashMap<Entry<IProject, Language>, List<IPath>>();
		for(URI file : files) {
			try {
				IPath path = relativePath(file);
				if(path != null) {
					final IProject project = EditorIOAgent.getProject(new File(file));
					final Language lang = LanguageRegistry.findLanguage(path, null);
					final Entry<IProject, Language> entry = new AbstractMap.SimpleEntry<IProject, Language>(project, lang);
					List<IPath> relativePaths = projects.get(entry);
					if(relativePaths == null) {
						relativePaths = new ArrayList<IPath>();
						projects.put(entry, relativePaths);
					}
					relativePaths.add(path);
				}
			} catch(FileNotFoundException e) {
				// Ignore exception, path is not added.
			}
		}

		for(Entry<Entry<IProject, Language>, List<IPath>> entry : projects.entrySet()) {
			StrategoAnalysisQueueFactory.getInstance().queueAnalysis(entry.getValue().toArray(new IPath[0]),
				entry.getKey().getKey(), triggerOnSave);
		}
	}

	private IPath relativePath(URI file) throws FileNotFoundException {
		assert file.isAbsolute();

		final IProject project = EditorIOAgent.getProject(new File(file));
		final IPath path = new Path(file.getPath());
		if(LanguageRegistry.findLanguage(path, null) != null) {
			IPath relPath = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
			assert !relPath.isAbsolute();
			return relPath;
		}

		return null;
	}

	/**
	 * Notify changes of all files in a project, as long as they are known to have an index associated with them.
	 */
	public void notifyNewProject(URI project) {
		Debug.log("Loading uninitialized project ", project);
		notifyNewProjectFiles(new File(project));
	}

	private void notifyNewProjectFiles(File file) {
		notifyChanges(getProjectFiles(file), true);
	}

	private Collection<URI> getProjectFiles(File file) {
		final List<URI> files = new LinkedList<URI>();
		if(file.isFile()) {
			if(isIndexedFile(new Path(file.getAbsolutePath()))) {
				files.add(file.toURI());
			}
		} else {
			final File[] subfiles = file.listFiles();
			if(subfiles != null) {
				for(File child : file.listFiles()) {
					files.addAll(getProjectFiles(child));
				}
			}
		}
		return files;
	}

	public static boolean isIndexedFile(IPath path) {
		Language language = LanguageRegistry.findLanguage(path, null);
		return language != null && IndexManager.isKnownIndexingLanguage(language.getName());
	}
}
