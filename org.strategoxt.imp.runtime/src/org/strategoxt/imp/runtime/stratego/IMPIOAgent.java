package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.spoofax.interpreter.library.IOAgent;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPIOAgent extends IOAgent {
	
	private Descriptor descriptor;
	
	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	@Override
	public InputStream openInputStream(String path, boolean isSourceRelative)
			throws FileNotFoundException {
		
		if (isSourceRelative) {
			return openAttachedFile(path);
		} else {
			return super.openInputStream(path, isSourceRelative);
		}
	}
	
	private InputStream openAttachedFile(String path) throws FileNotFoundException {
		String filename = new File(path).getName();
		return descriptor.openAttachment(filename);		
	}
	
	@Override
	protected String adaptFilePath(String fn) { // TODO: Something with IOAgent paths?
		return super.adaptFilePath(fn);
	}
}
