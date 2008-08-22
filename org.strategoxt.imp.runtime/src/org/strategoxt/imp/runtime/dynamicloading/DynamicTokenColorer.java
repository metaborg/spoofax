package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicTokenColorer extends DynamicService<ITokenColorer> implements ITokenColorer {

	public DynamicTokenColorer() {
		super(ITokenColorer.class);
	}
	
	public IRegion calculateDamageExtent(IRegion seed) {
		if (!isInitialized()) return seed;
		
		return getWrapped().calculateDamageExtent(seed);
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		initialize(controller.getLanguage());
		
		return getWrapped().getColoring(controller, token);
	}
}
