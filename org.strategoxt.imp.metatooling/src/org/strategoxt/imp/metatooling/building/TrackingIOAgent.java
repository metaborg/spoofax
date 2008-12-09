package org.strategoxt.imp.metatooling.building;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TrackingIOAgent extends EditorIOAgent {
	private Set<String> tracked = new HashSet<String>();
	
	public Set<String> getTracked() {
		return tracked;
	}
	
	private void track(String fn) {
		tracked.add(getAbsolutePath(fn));
	}
	
	@Override
	public File openFile(String fn) {
		track(fn);
		return super.openFile(fn);
	}
	
	@Override
	public InputStream openInputStream(String fn, boolean isInternalFile)
			throws FileNotFoundException {
		track(fn);
		return super.openInputStream(fn, isInternalFile);
	}
	
	@Override
	public int openRandomAccessFile(String fn, String mode)
			throws FileNotFoundException {
		track(fn);
		return super.openRandomAccessFile(fn, mode);
	}
}
