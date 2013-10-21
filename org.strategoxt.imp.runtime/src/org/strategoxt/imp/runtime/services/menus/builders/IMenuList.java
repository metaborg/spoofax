package org.strategoxt.imp.runtime.services.menus.builders;

import java.util.List;

import org.eclipse.imp.language.ILanguageService;

public interface IMenuList extends ILanguageService {

	List<Menu> getAll();
	
	Menu get(String menuName);
	
	IBuilder getBuilder(List<Integer> path);
}
