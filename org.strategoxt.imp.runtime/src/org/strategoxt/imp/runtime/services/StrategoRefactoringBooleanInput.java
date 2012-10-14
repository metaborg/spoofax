package org.strategoxt.imp.runtime.services;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.Environment;

public class StrategoRefactoringBooleanInput implements IStrategoRefactoringInput{

	protected final String labelText;
	protected final boolean defaultValue;
	protected boolean inputValue;

	public StrategoRefactoringBooleanInput(String label, boolean defaultValue) {
		this.labelText = label;
		this.defaultValue = defaultValue;
		this.inputValue = defaultValue;
	}

	public void setInputArea(Composite result, ModifyListener modListener) {
		//Label label= new Label(result, SWT.NONE);
		//label.setText(labelText);
		final Button booleanField = new Button(result, SWT.CHECK  | SWT.LEFT | SWT.BORDER); 
		booleanField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		booleanField.setText(labelText);
		booleanField.setSelection(defaultValue);
		inputValue = defaultValue;
		booleanField.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				inputValue = booleanField.getSelection();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				inputValue = booleanField.getSelection();
			}
		});
	}

	public IStrategoTerm getInputValue() {
		ITermFactory factory = Environment.getTermFactory();
		if(inputValue)
			return factory.makeInt(1);
		return factory.makeInt(0);
	}

	public RefactoringStatus validateUserInput() {
		return new RefactoringStatus();
	}
}