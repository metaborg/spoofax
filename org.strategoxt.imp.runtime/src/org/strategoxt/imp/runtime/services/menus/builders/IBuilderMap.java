package org.strategoxt.imp.runtime.services.menus.builders;

import java.util.Set;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IBuilderMap {
	
	Set<IBuilder> getAll();
	
	IBuilder get(String name);
}
