package org.strategoxt.imp.runtime.services.menus;

import java.util.List;

import org.eclipse.imp.language.ILanguageService;
import org.strategoxt.imp.runtime.services.menus.contribs.IBuilder;
import org.strategoxt.imp.runtime.services.menus.contribs.Menu;

public interface IMenuList extends ILanguageService {

	List<Menu> getAll();
	
	Menu get(String menuName);
	
	IBuilder getBuilder(List<String> path);
}
