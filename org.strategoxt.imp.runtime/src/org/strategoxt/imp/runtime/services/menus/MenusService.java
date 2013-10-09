package org.strategoxt.imp.runtime.services.menus;

import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * @author Oskar van Rest
 */
public class MenusService extends IMenusService {

	public MenusService(Descriptor descriptor) {
		try {
			System.out.println(descriptor.getLanguage().getName());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
	}
}
