package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.Environment;

public class StrategoRefactoringTextInput implements IStrategoRefactoringInput{

	protected final String labelText;
	protected final String defaultValue;
	protected String inputValue;

	public StrategoRefactoringTextInput(String label, String defaultValue) {
		this.labelText = label;
		this.defaultValue = defaultValue;
		this.inputValue = defaultValue;
	}

	public void setInputArea(Composite result, ModifyListener modListener) {
		Label label= new Label(result, SWT.NONE);
		label.setText(labelText);
		final Text textField = new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textField.setText(defaultValue);
		inputValue = defaultValue;
		textField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				inputValue = textField.getText();
			}
		});
		if(modListener != null){
			textField.addModifyListener(modListener);
		}
	}

	public IStrategoTerm getInputValue() {
		ITermFactory factory = Environment.getTermFactory();
		return factory.makeString(inputValue);
	}

	public RefactoringStatus validateUserInput() {
		return new RefactoringStatus();
	}
}