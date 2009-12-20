package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.compat.CompatLibrary;
import org.strategoxt.lang.compat.sglr.SGLRCompatLibrary;
import org.strategoxt.stratego_sglr.implode_asfix_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRLibrary extends SGLRCompatLibrary {
	
	private static boolean hasImplodeOverride;
	
	private final SourceMappings mappings = new SourceMappings();

	public IMPJSGLRLibrary(SGLRCompatLibrary sglrLibrary) {
		super(Environment.getATermFactory());
		
		add(new IMPParseStringPTPrimitive(Environment.getATermFactory(), sglrLibrary.getFilterSettings(), mappings));
		add(new IMPAnnoLocationPrimitive(mappings));
	}
	
	public void addOverrides(Context context) {
		if (!hasImplodeOverride) {
			implode_asfix_1_0.instance = new IMPImplodeAsfixStrategy();
			hasImplodeOverride = true;
		}
		
		((SSLLibrary) context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME)).add(new IMPOpenFile(mappings));
		((CompatLibrary) context.getOperatorRegistry(CompatLibrary.REGISTRY_NAME)).add(new IMPReadTextFromStream(mappings));
	}
	
	public SourceMappings getMappings() {
		return mappings;
	}

}
