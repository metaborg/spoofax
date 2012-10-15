package org.strategoxt.imp.runtime.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.spoofax.jsglr.client.KeywordRecognizer;

/**
 * @author Maartje de Jonge
 */
public class StrategoRefactoringIdentifierInput extends StrategoRefactoringTextInput  {
	
	private final Pattern idPattern;
	private final KeywordRecognizer keywordRecognizer;
	private final String languageName;
	
	public StrategoRefactoringIdentifierInput(
			String label, 
			String defaultValue,
			Pattern idPattern, 
			KeywordRecognizer keywordRecognizer,
			String languageName
	){
		super(label, defaultValue);
		this.idPattern = idPattern;
		this.keywordRecognizer = keywordRecognizer;
		this.languageName = languageName;
	}
	
	/* (non-Javadoc)
	 * @see org.strategoxt.imp.runtime.services.IStrategoRefactoringInput#validateUserInput()
	 */
	@Override
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
}
