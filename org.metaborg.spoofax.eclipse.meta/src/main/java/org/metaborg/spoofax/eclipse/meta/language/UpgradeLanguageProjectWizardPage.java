package org.metaborg.spoofax.eclipse.meta.language;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class UpgradeLanguageProjectWizardPage extends WizardPage {
    private final String languageName;
    private final String packageName;

    public Text inputLanguageName;
    public Text inputPackageName;


    public UpgradeLanguageProjectWizardPage(String languageName, String packageName) {
        super("wizardPage");

        this.languageName = languageName;
        this.packageName = packageName;

        setTitle("Upgrade language project");
        setDescription("This wizard upgrades a language project to the newest version");
    }


    @Override public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NULL);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        new Label(container, SWT.NULL).setText("&Language name:");
        inputLanguageName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputLanguageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputLanguageName.setText(languageName);

        new Label(container, SWT.NULL).setText("&Package name:");
        inputPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputPackageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputPackageName.setText(packageName);

        container.setLayout(layout);
        setControl(container);
        setPageComplete(true);
    }
}
