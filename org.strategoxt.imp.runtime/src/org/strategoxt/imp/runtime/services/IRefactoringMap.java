package org.strategoxt.imp.runtime.services;

import java.util.Set;

import org.eclipse.imp.language.ILanguageService;

/**
 * @author Maartje de Jonge
 */
public interface IRefactoringMap extends ILanguageService {
	
	Set<IRefactoring> getAll();
	
	IRefactoring get(String name);
}
