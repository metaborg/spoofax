package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	public static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new DialogPrimitive());
		add(new SubtermPrimitive());
		add(new ProjectPathPrimitive());
		add(new PluginPathPrimitive());
		add(new RefreshResourcePrimitive());
		add(new OriginLocationPrimitive());
		add(new OriginOffsetPrimitive());
		add(new OriginStripPrimitive());
		add(new OriginTermPrimitive());
		add(new OriginSublistTermPrimitive());
		add(new OriginTextPrimitive());
		add(new OriginTextFragmentPrimitive());
		add(new OriginEqualPrimitive());
		add(new OriginSurroundingCommentsPrimitive());
		add(new OriginCommentBeforePrimitive());
		add(new OriginCommentAfterPrimitive());
		add(new TextChangePrimitive());
		add(new OriginSourceTextPrimitive());
		add(new OriginPositionPrimitive());
		add(new OriginRootPrimitive());
		add(new OriginOffsetWithLayoutPrimitive());
		add(new QueueAnalysisPrimitive());
		add(new QueueStrategyPrimitive());
		add(new SetMarkersPrimitive());
		add(new CandidateSortsPrimitive());
		
		add(new SetTotalWorkUnitsPrimitive());
		add(new CompleteWorkUnitPrimitive());
		add(new OriginIndentationPrimitive());
		add(new OriginSeparatingWhitespacePrimitive());
		add(new OriginSeparatorPrimitive());
		add(new OriginSeparatorWithLayoutPrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
