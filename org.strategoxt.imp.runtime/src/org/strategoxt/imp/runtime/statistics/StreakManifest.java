/**
 * 
 */
package org.strategoxt.imp.runtime.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class StreakManifest {

	protected static final String PROP_TOTAL_STREAKS = "numstreaks";

	protected static final String PROP_PREFIX_STREAK = "streak.";

	protected static final String PROP_SUFFIX_FILES = ".files";

	private final File manifestFile;

	private Properties props;

	private StreakManifest(IPath project) {
		assert project != null;
		final File projectDir = project.toFile();
		assert projectDir.exists();
		final File streaksDir = new File(projectDir, EditStreakRecorder.RECORD_LOCATION);
		if (!streaksDir.exists() || streaksDir.isFile()) {
			streaksDir.mkdir();
		}
		manifestFile = new File(streaksDir, "streaks.mf").getAbsoluteFile();
		load();
	}

	private void load() {
		FileInputStream fis = null;
		try {
			if (!manifestFile.exists()) {
				manifestFile.createNewFile();
			}
			fis = new FileInputStream(manifestFile);
			props = new Properties();
			props.load(fis);
		} catch (IOException e) {
			;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				;
			}
		}
	}

	public int addStreak(EditStreak streak) {
		assert streak != null;
		final int totalStreaks = Integer.parseInt(props.getProperty(PROP_TOTAL_STREAKS, "0")) + 1;

		final Set<IResource> files = streak.getAffectedFiles();
		assert files.size() > 0;
		final Iterator<IResource> fiter = files.iterator();
		final StringBuilder streakFilesStr = new StringBuilder();
		while (fiter.hasNext()) {
			final IResource f = fiter.next();
			streakFilesStr.append(f.getProjectRelativePath().toString());
			if (fiter.hasNext()) {
				streakFilesStr.append(",");
			}
		}
		props.put(PROP_PREFIX_STREAK + totalStreaks + PROP_SUFFIX_FILES, streakFilesStr.toString());
		props.put(PROP_TOTAL_STREAKS, totalStreaks + "");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(manifestFile);
			props.store(fos, "");
		} catch (IOException e) {
			;
		}
		return totalStreaks;
	}

	public static StreakManifest getManifest(IPath project) {
		assert project != null;
		return new StreakManifest(project);
	}

}
