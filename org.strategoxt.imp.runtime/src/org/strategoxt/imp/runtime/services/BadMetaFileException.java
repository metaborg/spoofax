package org.strategoxt.imp.runtime.services;

public class BadMetaFileException extends Exception {
	private static final long serialVersionUID = -4173696243409037945L;
	
	public BadMetaFileException(String file) {
		super("Illegal or unrecognized .meta file: " + file);
	}
}
