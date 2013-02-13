package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.spoofax.interpreter.library.index.FilePartition;
import org.spoofax.interpreter.library.index.INotificationService;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.library.index.NotificationCenter;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoAnalysisQueueFactory;

/**
 * A {@link INotificationService} that uses {@link StrategoAnalysisQueueFactory}
 * .
 * 
 * Receives notifications sent by {@link FileNotificationServer} to the
 * {@link NotificationCenter}.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class QueueAnalysisService implements INotificationService {

	public void notifyChanges(URI file, String subfile, boolean triggerOnSave) {
		try {
			IProject project = EditorIOAgent.getProject(new File(file));
			IPath relPath = relativePath(file, subfile);

			// UNDONE to mitigate effects of http://yellowgrass.org/issue/Spoofax/262.
			// Don't schedule analysis for a file in the active editor.
			//if(isActiveEditor(file))
			//	return;
			
			StrategoAnalysisQueueFactory.getInstance().queueAnalysis(relPath, project, triggerOnSave);
		} catch (FileNotFoundException e) {
			Environment.logException("Background language service failed", e);
		} catch (RuntimeException e) {
			Environment.logException("Background language service failed", e);
		}
	}

	public void notifyChanges(FilePartition[] files, boolean triggerOnSave) {
		if (files.length == 0)
			return;

		List<IPath> relativePaths = new ArrayList<IPath>(files.length);
		for (FilePartition file : files) {
			try {
				IPath path = relativePath(file.file, file.partition);
				if (path != null)
					relativePaths.add(path);
			} catch (FileNotFoundException e) {
				// Ignore exception, path is not added.
			}
		}

		try {
			// TODO: assuming all projects are the same, is that fine?
			IProject project = EditorIOAgent.getProject(new File(files[0].file));
			StrategoAnalysisQueueFactory.getInstance().queueAnalysis(
					relativePaths.toArray(new IPath[0]), project, triggerOnSave);
		} catch (FileNotFoundException e) {
			Environment.logException("Background language service failed", e);
		}
	}

	private IPath relativePath(URI file, String subfile) throws FileNotFoundException {
		assert file.isAbsolute();

		IProject project = EditorIOAgent.getProject(new File(file));
		IPath path = new Path(file.getPath());
		if (LanguageRegistry.findLanguage(path, null) != null) {
			IPath relPath = path.removeFirstSegments(path.matchingFirstSegments(project
					.getLocation()));
			assert !relPath.isAbsolute();
			return relPath;
		}

		return null;
	}
	
	@SuppressWarnings("unused")
	private boolean isActiveEditor(URI file) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(editor != null) {
			URI openFileURI = ResourceUtil.getFile(editor.getEditorInput()).getLocationURI();
			return openFileURI.equals(file);
		}
		return false;
	}

	/**
	 * Notify changes of all files in a project, as long as they are known to
	 * have an index associated with them.
	 */
	public void notifyNewProject(URI project) {
		Debug.log("Loading uninitialized project ", project);
		notifyNewProjectFiles(new File(project));
	}

	private void notifyNewProjectFiles(File file) {
		notifyChanges(getProjectFileSubfiles(file).toArray(new FilePartition[0]), true);
	}

	private List<FilePartition> getProjectFileSubfiles(File file) {
		List<FilePartition> fileSubfiles = new ArrayList<FilePartition>();
		if (file.isFile()) {
			if (isIndexedFile(new Path(file.getAbsolutePath()))) {
				fileSubfiles.add(new FilePartition(file.toURI(), null));
			}
		} else {
			for (File child : file.listFiles()) {
				fileSubfiles.addAll(getProjectFileSubfiles(child));
			}
		}
		return fileSubfiles;
	}

	public static boolean isIndexedFile(IPath path) {
		Language language = LanguageRegistry.findLanguage(path, null);
		return language != null && IndexManager.isKnownIndexingLanguage(language.getName());
	}
}
