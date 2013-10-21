package org.strategoxt.imp.runtime.services.menus.builders;

import java.util.List;

public class MenuList implements IMenuList {

	private final List<Menu> menus;

	public MenuList(List<Menu> menus) {
		this.menus = menus;
	}

	public List<Menu> getAll() {
		return menus;
	}

	public Menu get(String name) {
		for (Menu menu : getAll()) {
			if (menu.getCaption().equals(name))
				return menu;
		}
		return null;
	}

	@Override
	public IBuilder getBuilder(List<String> path) {
		if (path.size() <= 2) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<IMenuContribution> menus = (List<IMenuContribution>) (List<?>) this.menus;
		Menu current = (Menu) Menu.findMenuContribution(menus, path.get(0));

		for (int i = 1; i < path.size() - 1; i++) {
			IMenuContribution menuContrib = Menu.findMenuContribution(current.getMenuContributions(), path.get(i));
			if (menuContrib instanceof Menu) {
				current = (Menu) menuContrib;
			} else {
				return null;
			}
		}

		IMenuContribution menuContrib = Menu.findMenuContribution(current.getMenuContributions(), path.get(path.size() - 1));
		if (menuContrib instanceof IBuilder) {
			return (IBuilder) menuContrib;
		} else {
			return null;
		}
	}
}
