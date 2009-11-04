package org.strategoxt.imp.runtime.services;

import java.util.Set;

import org.eclipse.imp.language.ILanguageService;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilderMap extends ILanguageService {
	
	Set<IBuilder> getAll();
	
	IBuilder get(String name);
}
