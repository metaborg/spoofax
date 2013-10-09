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
public class MenusServiceFactory extends AbstractServiceFactory<IMenusService> {

	public MenusServiceFactory() {
		super(IMenusService.class, false);
	}

	@Override
	public IMenusService create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		return new MenusService(descriptor);
	}

	public static void eagerInit(Descriptor descriptor, IParseController parser, EditorState lastEditor) {
		try {
			System.out.println("eager: " + descriptor.getLanguage().getName());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
	}

}
