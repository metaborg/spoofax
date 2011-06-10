package org.strategoxt.imp.runtime.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.strategoxt.imp.runtime.Environment;

public class RefactoringPageTextField extends UserInputWizardPage {

	private final Pattern idPattern;
	private final KeywordRecognizer keywordRecognizer;
	
	private Text identifierField;
	private String labelText = "&New name:";
	private String defaultName = "";

	public RefactoringPageTextField(Pattern idPattern, KeywordRecognizer keywordRecognizer) {
		super("SpoofaxRefactoringInputPage");
		this.idPattern = idPattern; 
		this.keywordRecognizer = keywordRecognizer;
	}

	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		result.setLayout(layout);
		Label label= new Label(result, SWT.NONE);
		label.setText(labelText);

		identifierField= createNameField(result);
		identifierField.setText(defaultName);
		identifierField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});
		identifierField.setFocus();
		identifierField.selectAll();
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
		RefactoringStatus status = validateUserInput();
		//set message
		int severity= status.getSeverity();
		String message= status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO && message != "") {
			setMessage(message, severity);
		} else {
			setMessage("", NONE);
		}
		setPageComplete(!status.hasError());
		
		//set userinput value
		StrategoRefactoring refactoring = getStrategoRefactoring();
		ITermFactory factory = Environment.getTermFactory();
		refactoring.setUserInputTerm(factory.makeString(identifierField.getText()));
	}

	private RefactoringStatus validateUserInput() {
		String inputString = identifierField.getText();
		if (idPattern != null){
			Matcher matcher = idPattern.matcher(inputString);
			if(!(matcher.matches())){
				if(inputString.trim().equals("")){
					return RefactoringStatus.createErrorStatus(""); //suppress error messages for empty fields
				}
				String errorMessage = "Name should match identifier pattern '" + 
				idPattern.pattern() + "', defined in <myLanguage>-Syntax.esv file."; 
				return RefactoringStatus.createErrorStatus(errorMessage);
			}
		}
		if(keywordRecognizer !=null &&  keywordRecognizer.isKeyword(inputString)){
			String errorMessage = "This name is used as a keyword.";					
			return RefactoringStatus.createErrorStatus(errorMessage);
		}
		//todo: warnings for all empty fields?
		return new RefactoringStatus();
	}
}
