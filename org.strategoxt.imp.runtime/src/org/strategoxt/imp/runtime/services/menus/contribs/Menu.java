package org.strategoxt.imp.runtime.services.menus.contribs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

public class Menu implements IMenuContribution {

	private final String caption;
	private final ImageDescriptor icon;
	private final List<IMenuContribution> menuContribs = new LinkedList<IMenuContribution>();
	
	public Menu(String caption) {
		this(caption, null);
	}
	
	public Menu(String caption, ImageDescriptor icon) {
		this.caption = caption;
		this.icon = icon;
	}
	
	@Override
	public String getCaption() {
		return caption;
	}
	
	public ImageDescriptor getIcon() {
		return icon;
	}

	public void addMenuContribution(IMenuContribution menuContrib) {
		menuContribs.add(menuContrib);
	}
	
	public List<IMenuContribution> getMenuContributions() {
		return menuContribs;
	}
	
	public IMenuContribution getMenuContribution(String caption) {
		return findMenuContribution(menuContribs, caption);
	}
	
	public static IMenuContribution findMenuContribution(List<IMenuContribution> menuContribs, String caption) {
		for (IMenuContribution contrib : menuContribs) {
			if (contrib.getCaption().equals(caption)) {
				return contrib;
			}
		}
		return null;
	}

	@Override
	public int getContributionType() {
		return IMenuContribution.MENU;
	}
}
