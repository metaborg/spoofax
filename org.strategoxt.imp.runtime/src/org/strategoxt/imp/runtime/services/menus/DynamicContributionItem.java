package org.strategoxt.imp.runtime.services.menus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.strategoxt.imp.runtime.services.menus.contribs.IBuilder;
import org.strategoxt.imp.runtime.services.menus.contribs.IMenuContribution;
import org.strategoxt.imp.runtime.services.menus.contribs.Menu;

/**
 * @author Oskar van Rest
 */
public class DynamicContributionItem extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator serviceLocator;

	public DynamicContributionItem() {
	}

	public DynamicContributionItem(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		LinkedList<IContributionItem> result = new LinkedList<IContributionItem>();

		int menuIndex = Integer.parseInt(getId().replaceAll(MenusServiceConstants.DYNAMIC_CONTRIBUTION_ITEM_ID_PREFIX, ""));

		MenuList menus = MenusServiceUtil.getMenus();
		if (menus.getAll().size() > menuIndex) {
			Menu menu = menus.getAll().get(menuIndex);

			for (IMenuContribution contrib : menu.getMenuContributions()) {
				switch (contrib.getContributionType()) {
				case IMenuContribution.BUILDER:
					IBuilder builder = (IBuilder) contrib;
					Map<String, String> params = new HashMap<>();
					params.put(MenusServiceConstants.PATH_PARAM, builder.getPath().toString());
					ImageDescriptor icon = null;
					CommandContributionItemParameter itemParams = new CommandContributionItemParameter(serviceLocator, builder.getPath().toString(), MenusServiceConstants.ACTION_ID, params, icon, null, null, builder.getCaption(), null, null, CommandContributionItem.STYLE_PUSH, null, true);
					result.add(new CommandContributionItem(itemParams));
					break;
				case IMenuContribution.SEPARATOR:
					result.add(new Separator());
					break;
				default:
					break;
				}
			}
		}

		return result.toArray(new IContributionItem[result.size()]);
	}

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	MenuList menus;

	@Override
	public boolean isDirty() {
		MenuList menus = MenusServiceUtil.getMenus();
		if (this.menus != menus) {
			this.menus = menus;
			return true;
		}
		return false;
	}
}
