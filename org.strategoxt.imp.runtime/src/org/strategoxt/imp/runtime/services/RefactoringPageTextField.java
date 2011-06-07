package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.Environment;

public class RefactoringPageTextField extends UserInputWizardPage {

	private Text fNameField;
	private String labelText = "&New name:";
	String defaultName = "<newname>";

	public RefactoringPageTextField(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		result.setLayout(layout);
		Label label= new Label(result, SWT.NONE);
		label.setText(labelText);

		fNameField= createNameField(result);
		fNameField.setText(defaultName);
		fNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});
		fNameField.setFocus();
		fNameField.selectAll();
		handleInputChanged();
	}

	private Text createNameField(Composite result) {
		Text field= new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private StrategoRefactoring getStrategoRefactoring() {
		return (StrategoRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		RefactoringStatus status= new RefactoringStatus();
		StrategoRefactoring refactoring = getStrategoRefactoring();

		ITermFactory factory = Environment.getTermFactory();
		refactoring.setUserInputTerm(factory.makeString(fNameField.getText()));
		//check all input fields
		setPageComplete(!status.hasError());
		int severity= status.getSeverity();
		String message= status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}
}
