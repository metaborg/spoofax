package org.strategoxt.imp.metatooling.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class UpgradeSpoofaxProjectWizardPage extends WizardPage {
	public UpgradeSpoofaxProjectWizardPage() {
		super("wizardPage");
		setTitle("Spoofax Project Upgrade");
		setDescription("This wizard detects outdated generated files of Spoofax projects and upgrades them.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		setControl(container);
		setPageComplete(true);
	}
}
