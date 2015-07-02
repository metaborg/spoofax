package org.metaborg.spoofax.core.completion;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageFacet;

import com.google.common.collect.Multimap;

public class CompletionFacet implements ILanguageFacet {
	private static final long serialVersionUID = -5048070012531904130L;

	private final Multimap<Integer, CompletionDefinition> completionDefinitions;


	public CompletionFacet(Multimap<Integer, CompletionDefinition> completionDefinitions) {
		this.completionDefinitions = completionDefinitions;
	}


	public @Nullable Iterable<CompletionDefinition> get(int s) {
		return completionDefinitions.get(s);
	}
}
