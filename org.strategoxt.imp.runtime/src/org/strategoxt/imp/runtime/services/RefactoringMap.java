package org.strategoxt.imp.runtime.services;

import java.util.Set;

import org.eclipse.imp.language.ILanguageService;

/**
 * @author Maartje de Jonge
 */
public class RefactoringMap implements IRefactoringMap, ILanguageService {
	
	private final Set<IRefactoring> Refactorings;
	
	public RefactoringMap(Set<IRefactoring> Refactorings) {
		this.Refactorings = Refactorings;
	}
	
	public Set<IRefactoring> getAll() {
		return Refactorings;
	}
	
	public IRefactoring get(String name) {
		for (IRefactoring Refactoring : getAll()) {
			if (Refactoring.getCaption().equals(name))
				return Refactoring;
		}
		return null;
	}
}
