package org.strategoxt.imp.runtime.dynamicloading;


import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.text.JTextComponent.KeyBinding;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.KeywordRecognizer;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.IRefactoring;
import org.strategoxt.imp.runtime.services.IRefactoringMap;
import org.strategoxt.imp.runtime.services.RefactoringMap;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoRefactoring;
import org.strategoxt.imp.runtime.services.StrategoRefactoringIdentifierInput;
import org.strategoxt.imp.runtime.services.SyntaxProperties;

public class RefactoringFactory extends AbstractServiceFactory<IRefactoringMap> {

	public RefactoringFactory() {
		super(IRefactoringMap.class, false); // not cached; depends on derived editor relation
	}

	@Override
	public IRefactoringMap create(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		Set<IRefactoring> refactorings = collectRefactorings(descriptor, controller);
		return new RefactoringMap(refactorings);
	}

	private static Set<IRefactoring> collectRefactorings(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		Set<IRefactoring> refactorings = new LinkedHashSet<IRefactoring>();
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		IStrategoAppl ppStrategyTerm = TermReader.findTerm(d.getDocument(), "PrettyPrint");
		String ppStrategy = null;
		if(ppStrategyTerm != null)
			ppStrategy=termContents(termAt(ppStrategyTerm, 0));
		else
			ppStrategy = ("pp-" + d.getLanguage().getName() + "-string").toLowerCase();
		for (IStrategoAppl aRefactoring : collectTerms(d.getDocument(), "Refactoring")) {
			IStrategoTerm[] semanticNodes = termAt(aRefactoring,0).getAllSubterms();
			String caption = termContents(termAt(aRefactoring, 1));
			String strategy = termContents(termAt(aRefactoring, 2));
			IStrategoList options = termAt(aRefactoring, 3);
			IStrategoTerm userInteractions = termAt(aRefactoring, 4);
			ArrayList<StrategoRefactoringIdentifierInput> inputFields = 
				getInputFields(userInteractions, controller.getEditor());
			String actionDefinitionId = getKeyBinding(userInteractions);
			//actionDefinitionId = "org.eclipse.jdt.ui.edit.text.java.rename.element";
			boolean cursor = false;
			boolean source = false;
			boolean meta = false;
			for (IStrategoTerm option : options.getAllSubterms()) {
				String type = cons(option);
				if (type.equals("Cursor")) {
					cursor = true;
				} else if (type.equals("Source")) {
					source = true;
				} else if (type.equals("Meta")) {
					meta = true;
				} else if (
						type.equals("OpenEditor") ||
						type.equals("RealTime") ||
						type.equals("Persistent")
					){
					Environment.logWarning("Unused builder annotation '"+ type + "' in '" + caption +"'");
				}
				else {
					throw new BadDescriptorException("Unknown builder annotation: " + type);
				}
			}
			if (!meta || d.isDynamicallyLoaded()){			
				refactorings.add(
					new StrategoRefactoring(
						feedback, 
						caption, 
						strategy,
						cursor, 
						source, 
						ppStrategy,
						semanticNodes,
						inputFields,
						actionDefinitionId
					)
				);
			}
		}
		return refactorings;
	}
	
	private static String getKeyBinding(IStrategoTerm userInteractions) {
		IStrategoTerm keybinding = TermReader.findTerm(userInteractions, "KeyBinding");
		if(keybinding != null)
			return TermReader.termContents(keybinding);
		IStrategoTerm interactionId = TermReader.findTerm(userInteractions, "InteractionId");
		if(interactionId != null)
			return TermReader.termContents(interactionId);
		return null;
	}

	private static ArrayList<StrategoRefactoringIdentifierInput> getInputFields(
			IStrategoTerm userInteractions, EditorState editor) {
		ArrayList<StrategoRefactoringIdentifierInput> inputFields = new ArrayList<StrategoRefactoringIdentifierInput>();
		IStrategoTerm userInput = TermReader.findTerm(userInteractions, "UserInput");
		if(userInput != null){
			IStrategoTerm userInputList = userInput.getSubterm(0);
			for (int i = 0; i < userInputList.getSubtermCount(); i++) {
				IStrategoAppl input = TermReader.applAt(userInputList, i);
				if(TermReader.hasConstructor(input, "IdInputField")){
					String label = termContents(termAt(input,0));
					String defaultValue = "";
					if(input.getSubtermCount() > 1)
						defaultValue = termContents(termAt(input,1)); //TODO: Strategy OR String
					StrategoRefactoringIdentifierInput idInput = 
						new StrategoRefactoringIdentifierInput(
							label, 
							defaultValue, 
							getIdPattern(editor), 
							getKeywordRecognizer(editor),
							getLanguageName(editor)
						);
					inputFields.add(idInput);
				}
				//TODO other input types
				//TODO pattern
			}
		}
		return inputFields;
	}

	/*
	private static ArrayList<StrategoRefactoringIdentifierInput> getInputFields(
			IStrategoAppl aRefactoring, EditorState editor) {
		ArrayList<StrategoRefactoringIdentifierInput> inputFields = new ArrayList<StrategoRefactoringIdentifierInput>();
		//TODO: read them from builder
		StrategoRefactoringIdentifierInput idInput1 = 
			new StrategoRefactoringIdentifierInput(
				"New name", 
				"", 
				getIdPattern(editor), 
				getKeywordRecognizer(editor),
				getLanguageName(editor)
			);
		inputFields.add(idInput1);
		return inputFields;
	}*/

	private static String getLanguageName(EditorState editor) {
		try {
			return editor.getDescriptor().getLanguage().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return "<MyLanguage>";
		}
	}

	private static Pattern getIdPattern(EditorState editor) {
		Descriptor descriptor = editor.getDescriptor();
		SyntaxProperties syntax = null;
		if (descriptor != null) {
			try {
				syntax = (SyntaxProperties) descriptor.createService(ILanguageSyntaxProperties.class, null);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not read syntax properties", e);
				e.printStackTrace();
			}
		} 
		return syntax != null ? syntax.getIdentifierLexical() : null;
	}

	private static KeywordRecognizer getKeywordRecognizer(EditorState editor) {
		try {
			return editor.getParseController().getParser().getParseTable().getKeywordRecognizer();
		}
		catch (Exception e){
			Environment.logException("Could not fetch keyword recognizer", e);
			e.printStackTrace();
			return null;
		}
	}

}
