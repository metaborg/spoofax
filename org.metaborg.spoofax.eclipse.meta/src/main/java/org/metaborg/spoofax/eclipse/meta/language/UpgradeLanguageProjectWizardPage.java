package org.metaborg.spoofax.eclipse.meta.language;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
        container.setLayout(layout);

        final Label description = new Label(container, SWT.NULL);
        description.setText("Language name and id are retrieved from the packed.esv file if it exists. "
            + "If it does not exist, please copy the language name and id from the main.esv file.");
        GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
        description.setLayoutData(gridData);

        new Label(container, SWT.NULL).setText("&Language name:");
        inputLanguageName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputLanguageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputLanguageName.setText(languageName);
        inputLanguageName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });

        new Label(container, SWT.NULL).setText("&Language id:");
        inputPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputPackageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        inputPackageName.setText(packageName);
        inputPackageName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                onChange();
            }
        });
        
        setControl(container);
        setPageComplete(false);
    }


    private void onChange() {
        if(inputLanguageName.getText().isEmpty() || inputPackageName.getText().isEmpty()) {
            setPageComplete(false);
        } else {
            setPageComplete(true);
        }
    }
}
