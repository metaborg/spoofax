package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPLibrary extends AbstractStrategoOperatorRegistry {
	
	public static final String REGISTRY_NAME = "sdf2imp";
	
	public IMPLibrary() {
		add(new NameDialogPrimitive());
		add(new SubtermPrimitive());
		add(new ProjectPathPrimitive());
		add(new PluginPathPrimitive());
		add(new RefreshResourcePrimitive());
		add(new QueueAnalysisPrimitive());
		add(new QueueStrategyPrimitive());
		add(new SetMarkersPrimitive());
		add(new CandidateSortsPrimitive());
		
		add(new SetTotalWorkUnitsPrimitive());
		add(new CompleteWorkUnitPrimitive());
		add(new SaveAllResourcesPrimitive());
		add(new MessageDialogPrimitive());
		add(new LanguageDescriptionPrimitive());
		add(new OverrideInputPrimitive());

		add(new InSelectedFragmentPrimitive());
		add(new OriginNonLayoutTokensPrimitive());

		//origin term strategies
		add(new OriginSublistTermPrimitive());
		add(new OriginDesugaredTermPrimitive());
		add(new OriginTermFuzzyPrimitive());
		
		//layout strategies
		add(new OriginSurroundingCommentsPrimitive());
		add(new OriginLayoutPrefixPrimitive());
		add(new OriginCommentsAfterPrimitive());
		add(new OriginCommentsBeforePrimitive());
		add(new OriginIndentationPrimitive());
		add(new OriginSeparationPrimitive());
		add(new OriginDeletionOffsetPrimitive());
		add(new OriginInsertBeforeOffsetPrimitive());
		add(new OriginInsertAtEndOffsetPrimitive());
		add(new OriginTextWithLayoutPrimitive());

		add(new OriginLanguagePrimitive());
	}

	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
