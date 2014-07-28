/**
 * 
 */
package org.strategoxt.imp.runtime.statistics;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class EditStreak {
	private final List<RecordedEdit> edits = new LinkedList<RecordedEdit>();

	private volatile long lastEdit = -1;

	public long getLastEdit() {
		if (edits.size() == 0) {
			return -1;
		}
		return lastEdit;
	}

	public void addEdit(RecordedEdit edit) {
		assert lastEdit <= edit.timestamp;
		edits.add(edit);
		lastEdit = edit.timestamp;
	}

	public void commit() {
		final Collection<EditStreak> streaks = splitPerProject();
		for (EditStreak editStreak : streaks) {
			try {
				editStreak.doCommit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doCommit() throws IOException {
		final IPath project = edits.get(0).project;
		final StreakManifest manifest = StreakManifest.getManifest(project);
		final int streakNumber = manifest.addStreak(this);

		final File streaksFolder = new File(project.toFile(), EditStreakRecorder.RECORD_LOCATION);
		final File recordFolder = new File(streaksFolder, streakNumber + "");
		if (!recordFolder.exists()) {
			recordFolder.mkdirs();
		}
		for (RecordedEdit edit : edits) {
			final IPath sFilePath = edit.resource.getProjectRelativePath();
			ensureDirectories(recordFolder, sFilePath.segments());
			final File outFile = new File(recordFolder, sFilePath.toString() + "._streak");
			final PrintStream ps = new PrintStream(outFile);
			ps.print(ImploderAttachment.getTokenizer(edit.ast).getInput());
			ps.close();
		}

	}

	private void ensureDirectories(File parentFolder, String[] segments) {
		for (int idx = 0; idx < segments.length - 1; idx++) {
			parentFolder = new File(parentFolder, segments[idx]);
		}
		parentFolder.mkdirs();
	}

	public Set<IResource> getAffectedFiles() {
		final Set<IResource> files = new HashSet<IResource>();
		for (RecordedEdit edit : edits) {
			files.add(edit.resource);
		}
		return files;
	}

	public void pack() {
		final HashMap<IResource, RecordedEdit> packetEdits = new HashMap<IResource, RecordedEdit>();
		for (RecordedEdit edit : edits) {
			packetEdits.put(edit.resource, edit);
		}
		edits.clear();
		edits.addAll(packetEdits.values());
	}

	private Collection<EditStreak> splitPerProject() {
		final HashMap<IPath, EditStreak> projectStreaks = new HashMap<IPath, EditStreak>();
		for (RecordedEdit edit : edits) {
			EditStreak streak = projectStreaks.get(edit.project);
			if (streak == null) {
				streak = new EditStreak();
				projectStreaks.put(edit.project, streak);
			}
			streak.addEdit(edit);
		}

		return projectStreaks.values();
	}
}
