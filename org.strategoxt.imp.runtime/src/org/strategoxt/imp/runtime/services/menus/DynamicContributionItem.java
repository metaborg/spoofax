package org.strategoxt.imp.runtime.services.menus;

import java.util.LinkedList;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @author Oskar van Rest
 */
public class DynamicContributionItem extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator serviceLocator;
	private boolean dirty;

	public DynamicContributionItem() {
	}

	public DynamicContributionItem(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		LinkedList<IContributionItem> result = new LinkedList<IContributionItem>();
		
		CommandContributionItemParameter dummyParams = new CommandContributionItemParameter(serviceLocator, "a", "org.spoofax.action", CommandContributionItem.STYLE_PUSH);
		result.add(new CommandContributionItem(dummyParams));
		
		/*
		 * TODO
		 * 
		 * editor = getActiveEditor()
		 * 
		 * if (editor != null && editor instanceof SpoofaxEditor)
		 *   SpoofaxMenusService sms = get SpoofaxMenusService for editor
		 *   for (menu : sms)
		 *     for (contrib : menu.contribs)
		 *       result.add(new contrib)
		 */
		
		return result.toArray(new IContributionItem[result.size()]);
	}

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
