package org.strategoxt.imp.runtime.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.strategoxt.imp.runtime.Environment;

public class StrategoRefactoringIdentifierInput {
	
	private final Pattern idPattern;
	private final KeywordRecognizer keywordRecognizer;
	private final String labelText;
	private final String defaultName;
	private final String languageName;
	private String inputValue;
	
	public StrategoRefactoringIdentifierInput(
			String label, 
			String defaultValue,
			Pattern idPattern, 
			KeywordRecognizer keywordRecognizer,
			String languageName
	){
		this.labelText = label;
		this.defaultName = defaultValue;
		this.inputValue = defaultValue;
		this.idPattern = idPattern;
		this.keywordRecognizer = keywordRecognizer;
		this.languageName = languageName;
	}
	
	public void setInputArea(Composite result, ModifyListener modListener) {
		Label label= new Label(result, SWT.NONE);
		label.setText(labelText);
		final Text identifierField = new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		identifierField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		identifierField.setText(defaultName);
		inputValue = defaultName;
		identifierField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				inputValue = identifierField.getText();
			}
		});
		if(modListener != null){
			identifierField.addModifyListener(modListener);
		}
	}
	
	public RefactoringStatus validateUserInput() {
		String inputString = inputValue;
		if (idPattern != null){
			Matcher matcher = idPattern.matcher(inputString);
			if(!(matcher.matches())){
				if(inputString.trim().equals("")){
					return RefactoringStatus.createErrorStatus(""); //suppress error messages for empty fields
				}
				String errorMessage = 
					"Value of '" + this.labelText + 
					"' should match identifier pattern '" + 
					idPattern.pattern() + "', defined in " + languageName + "-Syntax.esv file."; 
				return RefactoringStatus.createErrorStatus(errorMessage);
			}
		}
		if(keywordRecognizer !=null &&  keywordRecognizer.isKeyword(inputString)){
			String errorMessage = "Value of '" + this.labelText +"' is used as a keyword.";					
			return RefactoringStatus.createErrorStatus(errorMessage);
		}
		return new RefactoringStatus();
	}
	
	public IStrategoTerm getInputValue(){
		ITermFactory factory = Environment.getTermFactory();
		return factory.makeString(inputValue);
	}
}
