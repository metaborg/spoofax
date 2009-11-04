package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Set;

import org.strategoxt.imp.runtime.services.IBuilder;
import org.strategoxt.imp.runtime.services.IBuilderMap;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicBuilder extends AbstractService<IBuilderMap> implements IBuilderMap {

	public DynamicBuilder() {
		super(IBuilderMap.class);
	}

	public IBuilder get(String name) {
		return getWrapped().get(name);
	}

	public Set<IBuilder> getAll() {
		return getWrapped().getAll();
	}

}
