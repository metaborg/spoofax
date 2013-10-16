package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.ui.commands.ICommandService;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.AbstractServiceFactory;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Oskar van Rest
 */
public class MenusServiceFactory extends AbstractServiceFactory<MenusService> {

	public MenusServiceFactory() {
		super(MenusService.class, true);
	}

	@Override
	public MenusService create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		return new MenusService(descriptor);
	}

	public static void eagerInit(Descriptor descriptor, IParseController parser, EditorState lastEditor) {
//		IWorkbench wb = PlatformUI.getWorkbench();
//		ICommandService commandService = (ICommandService) wb.getService(ICommandService.class);
		ICommandService commandService = (ICommandService) lastEditor.getEditor().getSite().getService(ICommandService.class);
//		Map<Object, Object> filter = new HashMap<Object, Object>();
//		filter.put("org.spoofax.menus.menuEnabled", true);
		for (int i = 1; i <= MenusServiceConstants.NO_OF_TOOLBAR_MENUS; i++) {
			commandService.refreshElements(MenusServiceConstants.TOOLBAR_BASECOMMAND_ID_PREFIX + i, null);
		}
//		commandService.refreshElements(MenusServiceConstants.TOOLBAR_ID, null);
	}
}