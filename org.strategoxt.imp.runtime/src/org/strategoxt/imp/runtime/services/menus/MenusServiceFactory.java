package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.imp.parser.IParseController;
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
		// Refresh toolbar menu commands after rebuilding.
		MenusServiceUtil.refreshToolbarMenuCommands();
	}
}