package org.strategoxt.imp.runtime.services.menus;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.jface.action.ContributionItem;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * @author Oskar van Rest
 */
public class MenusService implements ILanguageService {
	
	private boolean toolbarMenuEnabled[] = new boolean[MenusServiceConstants.NO_OF_TOOLBAR_MENUS];
	
	private Collection<MenuContribution> menuContributions;

	public MenusService(Descriptor descriptor) {
		menuContributions = new LinkedList<MenuContribution>();
		
		System.out.println("new menuservice");
		// TODO fill menuContributions
	}
	
	public Collection<MenuContribution> getMenuContributions() {
		return menuContributions;
	}
	
	class MenuContribution {
		
		private String locationURI;
		private Collection<MenuCategoryContribution> menuCategoryContributions;
		
		public MenuContribution(String locationURI) {
			this.locationURI = locationURI;
			menuCategoryContributions = new LinkedList<MenuCategoryContribution>();
		}
		
		public String getLocationURI() {
			return locationURI;
		}
		
		public void addMenuCategoryContribution(MenuCategoryContribution contrib) {
			menuCategoryContributions.add(contrib);
		}
		
		public Collection<MenuCategoryContribution> getMenuCategoryContributions() {
			return menuCategoryContributions;
		}
	}
	
	class MenuCategoryContribution {
		
		private Collection<ContributionItem> contributionItems;
		
		public MenuCategoryContribution() {
			contributionItems = new LinkedList<ContributionItem>();
		}
		
		public void addMenuCategoryContribution(ContributionItem contrib) {
			contributionItems.add(contrib);
		}
		
		public Collection<ContributionItem> getMenuCategoryContributions() {
			return contributionItems;
		}
	}
}
