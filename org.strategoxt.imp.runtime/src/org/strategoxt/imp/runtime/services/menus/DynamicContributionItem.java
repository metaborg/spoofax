package org.strategoxt.imp.runtime.services.menus;

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
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.menus.builders.IMenuContribution;
import org.strategoxt.imp.runtime.services.menus.builders.Menu;
import org.strategoxt.imp.runtime.services.menus.builders.MenuList;

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

		MenuList menus = getMenus();
		if (menus.getAll().size() >= menuIndex) {
			Menu menu = menus.getAll().get(menuIndex);

			for (IMenuContribution contrib : menu.getMenuContributions()) {
				switch (contrib.getContributionType()) {
				case IMenuContribution.BUILDER:
					
//					IServiceLocator serviceLocator,
//					String id, String commandId, Map parameters, ImageDescriptor icon,
//					ImageDescriptor disabledIcon, ImageDescriptor hoverIcon,
//					String label, String mnemonic, String tooltip, int style,
//					String helpContextId, boolean visibleEnabled
					
					
					CommandContributionItemParameter dummyParams = new CommandContributionItemParameter(serviceLocator, "a", MenusServiceConstants.ACTION_ID, CommandContributionItem.STYLE_PUSH);
					result.add(new CommandContributionItem(dummyParams));
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
		MenuList menus = getMenus();
		if (this.menus != menus) {
			this.menus = menus;
			return true;
		}
		return false;
	}

	public MenuList getMenus() {
		EditorState activeEditor = EditorState.getActiveEditor();
		if (activeEditor != null) {
			try {
				return activeEditor.getDescriptor().createService(MenuList.class, activeEditor.getParseController());
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
