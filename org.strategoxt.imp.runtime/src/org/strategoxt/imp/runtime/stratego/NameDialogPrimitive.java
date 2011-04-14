package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.spoofax.jsglr.client.ParseTable;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.SyntaxProperties;

/**
 * @author Maartje de Jonge
 */
public class NameDialogPrimitive extends AbstractPrimitive {

	public NameDialogPrimitive() {
		super("SSL_EXT_newnamedialog", 0, 4);
	}
	
	@Override
	public boolean call(final IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {		
		
		if (fetchOverriddenValue(env))
			return true;
		
		for (int i = 0; i < tvars.length; i++) {
			if (!isTermString(tvars[i])) return false;			
		}
		final Language language = LanguageRegistry.findLanguage(asJavaString(tvars[0]));
		final IInputValidator idValidator = getIdentifierValidator(language);				
		final String title = ((IStrategoString)tvars[1]).stringValue(); 
		final String message = ((IStrategoString)tvars[2]).stringValue();
		final String input = ((IStrategoString)tvars[3]).stringValue();
		final  Boolean[] dialogResultOk={false};
		Job job = new UIJob("user input") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				InputDialog dialog = new InputDialog(null, title, message, input, idValidator);
				if (dialog.open() == InputDialog.OK) {
					String userInput=dialog.getValue();
					env.setCurrent(env.getFactory().makeString(userInput));
					dialogResultOk[0]=true;
				} 
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.SHORT);
		job.schedule();	
		try {
			job.join();
		} catch (InterruptedException e) {
			Environment.logException("Interrupted", e);
		}
		return job.getResult()==Status.OK_STATUS && dialogResultOk[0]==true;
	}

	private boolean fetchOverriddenValue(final IContext env) {
		IOperatorRegistry registry = env.getOperatorRegistry(IMPLibrary.REGISTRY_NAME);
		OverrideInputPrimitive override = (OverrideInputPrimitive) registry.get(OverrideInputPrimitive.NAME);
		String overridden = override.getOverrideValue();
		if (overridden != null) {
			env.setCurrent(env.getFactory().makeString(overridden));
			return true;
		} else {
			return false;
		}
	}

	private IInputValidator getIdentifierValidator(final Language language) {
		IInputValidator validator = null;
		if (language != null) {
			SyntaxProperties syntax = getSyntaxProperties(language);
			final Pattern idPattern = syntax.getIdentifierLexical();
			final KeywordRecognizer keywordRecognizer = getKeyWordRecognizer(language);
			validator = new IInputValidator() {
				public String isValid(String newText) {
					if(newText.equals("")){
						return ""; //disable but do not show error message for empty input 
					}
					if(idPattern != null){
						Matcher matcher = idPattern.matcher(newText);
						if(!(matcher.matches())){
							return "Error: name should match identifier pattern '" + 
							idPattern.pattern()+ "'. \n(" + language.getName() + "-Syntax.esv)";
						}
					}
					if(keywordRecognizer !=null &&  keywordRecognizer.isKeyword(newText)){
						return "Error: This name is used as a keyword in '" + language.getName() + "'";					
					}
					return null;
				}
			};
		}
		return validator;
	}

	private KeywordRecognizer getKeyWordRecognizer(Language language) {
		ParseTable pt = null;
		try {
			pt = Environment.getParseTableProvider(language).get();
			return pt.getKeywordRecognizer();
		} catch (Exception e1) {
			Environment.logException("Could not fetch keyword recognizer", e1);
			return null;
		} 
	}
	
	private static SyntaxProperties getSyntaxProperties(Language language) {
		SyntaxProperties result = null;
		Descriptor descriptor = Environment.getDescriptor(language);
		if (descriptor != null) {
			try {
				result = (SyntaxProperties) descriptor.createService(ILanguageSyntaxProperties.class, null);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not read syntax properties", e);
			}
		} else {
			result = (SyntaxProperties) ServiceFactory.getInstance().getSyntaxProperties(language);
		}
		return result != null ? result : new SyntaxProperties(null, null, null, null, null, null, null);
	}
}
