package org.strategoxt.imp.runtime.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class FileCopier {
	final byte[] buffer = new byte[4096];

	public void copyFile(File in, File out) throws IOException {
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			inStream = new FileInputStream(in);
			outStream = new FileOutputStream(out);
			int i = 0;
			while ((i = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, i);
			}
		} finally {
			if (inStream != null)
				inStream.close();
			if (outStream != null)
				outStream.close();
		}
	}
	
	public File copyToTempFile(File file) throws IOException {
		File result = File.createTempFile(file.getName() + "-", ".jar");
		copyFile(file, result);
		return result;
	}

}
