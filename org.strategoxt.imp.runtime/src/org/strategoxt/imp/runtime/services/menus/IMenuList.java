package org.strategoxt.imp.runtime.services.menus;

import java.util.List;

import org.eclipse.imp.language.ILanguageService;
import org.strategoxt.imp.runtime.services.menus.model.IBuilder;
import org.strategoxt.imp.runtime.services.menus.model.Menu;

public interface IMenuList extends ILanguageService {

	List<Menu> getAll();
	
	Menu get(String menuName);
	
	IBuilder getBuilder(List<String> path);
}
