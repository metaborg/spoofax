package org.strategoxt.imp.runtime.services.menus;

import java.util.LinkedList;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.editor.SpoofaxEditor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

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

		CommandContributionItemParameter dummyParams = new CommandContributionItemParameter(serviceLocator, "a", "org.spoofax.action", CommandContributionItem.STYLE_PUSH);
		result.add(new CommandContributionItem(dummyParams));

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
		return false;
	}
}
