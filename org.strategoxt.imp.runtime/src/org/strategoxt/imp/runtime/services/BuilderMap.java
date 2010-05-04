package org.strategoxt.imp.runtime.services;

import java.util.Set;

import org.eclipse.imp.language.ILanguageService;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BuilderMap implements IBuilderMap, ILanguageService {
	
	private final Set<IBuilder> builders;
	
	public BuilderMap(Set<IBuilder> builders) {
		this.builders = builders;
	}
	
	public Set<IBuilder> getAll() {
		return builders;
	}
	
	public IBuilder get(String name) {
		for (IBuilder builder : getAll()) {
			if (builder.getCaption().equals(name))
				return builder;
		}
		return null;
	}
}
