package org.strategoxt.imp.runtime.services.menus.contribs;

/**
 * @author Oskar van Rest
 */
public abstract class AbstractBuilder implements IBuilder{

	@Override
	public int getContributionType() {
		return IMenuContribution.BUILDER;
	}
}
