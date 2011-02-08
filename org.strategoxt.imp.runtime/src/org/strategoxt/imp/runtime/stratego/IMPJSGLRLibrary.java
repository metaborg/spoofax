package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.compat.CompatLibrary;
import org.strategoxt.lang.compat.sglr.SGLRCompatLibrary;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRLibrary extends SGLRCompatLibrary {
	
	private final SourceMappings mappings = new SourceMappings();

	public IMPJSGLRLibrary(SGLRCompatLibrary sglrLibrary) {
		add(new IMPParseStringPTPrimitive(sglrLibrary.getFilterSettings(), sglrLibrary.getRecoveryEnabledSetting(), mappings));
		add(new IMPParseStringPrimitive(sglrLibrary.getFilterSettings(), sglrLibrary.getRecoveryEnabledSetting(), mappings));
	}
	
	public void addOverrides(Context context) {
		((SSLLibrary) context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME)).add(new IMPOpenFile(mappings));
		((CompatLibrary) context.getOperatorRegistry(CompatLibrary.REGISTRY_NAME)).add(new IMPReadTextFromStream(mappings));
	}
	
	public SourceMappings getMappings() {
		return mappings;
	}

}
