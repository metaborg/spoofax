package org.strategoxt.imp.runtime.services.menus.contribs;

public class Separator implements IMenuContribution {

	@Override
	public String getCaption() {
		return "";
	}

	@Override
	public int getContributionType() {
		return IMenuContribution.SEPARATOR;
	}
}
