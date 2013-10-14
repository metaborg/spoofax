package org.strategoxt.imp.runtime.services.menus;

import java.util.LinkedList;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.IViewRegistry;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

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

		if (menuIndex == 1) {
			CommandContributionItemParameter dummyParams = new CommandContributionItemParameter(serviceLocator, "a", MenusServiceConstants.ACTION_ID, CommandContributionItem.STYLE_PUSH);
			result.add(new CommandContributionItem(dummyParams));
		}

		/*
		 * TODO
		 * 
		 * editor = getActiveEditor()
		 * 
		 * if (editor != null && editor instanceof SpoofaxEditor)
		 * SpoofaxMenusService sms = get SpoofaxMenusService for editor for
		 * (menu : sms) for (contrib : menu.contribs) result.add(new contrib)
		 */

		return result.toArray(new IContributionItem[result.size()]);
	}

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	MenusService menusService;

	@Override
	public boolean isDirty() {
		EditorState activeEditor = EditorState.getActiveEditor();
		if (activeEditor != null) {
			MenusService menusService = null;
			try {
				menusService = activeEditor.getDescriptor().createService(MenusService.class, activeEditor.getParseController());
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
			if (this.menusService != menusService) {
				this.menusService = menusService;
				return true;
			}
		}
		return true;
	}
}
