package org.strategoxt.imp.runtime.stratego;

import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.compat.sglr.SGLRCompatLibrary;
import org.strategoxt.stratego_sglr.implode_asfix_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRLibrary extends SGLRCompatLibrary {

	public IMPJSGLRLibrary(SGLRCompatLibrary sglrLibrary) {
		super(Environment.getWrappedATermFactory());
		
		add(new IMPParseStringPTPrimitive(Environment.getWrappedATermFactory(), sglrLibrary.getFilterSettings()));
		add(new IMPAnnoLocationPrimitive());
	}
	
	public static void init() {
		implode_asfix_1_0.instance = new IMPImplodeAsfixStrategy();
	}

}
