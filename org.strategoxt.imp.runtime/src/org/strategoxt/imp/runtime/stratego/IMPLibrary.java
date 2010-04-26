package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	public static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new SubtermPrimitive());
		add(new ProjectPathPrimitive());
		add(new RefreshResourcePrimitive());
		add(new OriginLocationPrimitive());
		add(new OriginCharPositionPrimitive());
		add(new OriginStripPrimitive());
		add(new OriginTermPrimitive());
		add(new OriginSublistTermPrimitive());
		add(new OriginTextPrimitive());
		add(new OriginTextFragmentPrimitive());
		add(new OriginEqualPrimitive());
		add(new OriginSurroundingCommentsPrimitive());
		add(new OriginLeftCommentLinesPrimitive());
		add(new OriginRightLineCommentPrimitive());
		add(new TextChangePrimitive());
		add(new OriginPositionToLocationPrimitive());
		add(new OriginSourceTextPrimitive());
		add(new SelectedFromToPrimitive());
		add(new OriginRootPrimitive());
		add(new OriginPositionLayoutIncludedPrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
