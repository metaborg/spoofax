package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRLibrary extends JSGLRLibrary {

	public IMPJSGLRLibrary() {
		super(Environment.getWrappedATermFactory());
		
		add(new IMPJSGLRParser());
	}

}
