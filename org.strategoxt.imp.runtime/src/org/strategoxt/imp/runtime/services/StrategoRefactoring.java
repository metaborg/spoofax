package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.generator.construct_textual_change_1_1;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class StrategoRefactoring extends Refactoring implements IRefactoring {
		
	private final String ppStrategy;
	
	private final StrategoObserver observer;

	private final String caption;
	
	private final String builderRule;
			
	private final boolean cursor;
	
	private final boolean source;
	
	private final IStrategoTerm[] semanticNodes;
			
	protected final ArrayList<StrategoRefactoringIdentifierInput> inputFields;

	public ArrayList<StrategoRefactoringIdentifierInput> getInputFields() {
		return inputFields;
	}

	private IStrategoTerm node;
	
	private Collection<TextFileChange> fileChanges;

	public boolean isDefinedOnSelection(EditorState editor) {
		// TODO Auto-generated method stub
		return getSelectionNode(editor) != null;
	}

	public String getCaption() {
		return caption;
	}

	public String getActionDefinitionId() {
		// TODO 
		return "org.eclipse.jdt.ui.edit.text.java.rename.element";
	}
	
	public void prepareExecute(EditorState editor) {
		this.node = getSelectionNode(editor);
		fileChanges.clear();
		//inputFields.clear(); set default values?
	}

	public StrategoRefactoring(StrategoObserver observer, String caption, String builderRule,
			boolean cursor, boolean source, String ppStrategy,
			IStrategoTerm[] semanticNodes, ArrayList<StrategoRefactoringIdentifierInput> inputFields) {
		this.cursor=cursor;
		this.source=source;
		this.ppStrategy=ppStrategy;
		this.observer = observer;
		this.caption = caption;
		this.builderRule = builderRule;
		this.semanticNodes = semanticNodes;
		fileChanges = new HashSet<TextFileChange>();
		this.inputFields = inputFields;
	}
	
	@Override
	public String getName() {
		return caption;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(node == null)
			status.merge(RefactoringStatus.createFatalErrorStatus("Refactoring is not defined for the current selection."));	
		return status;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		final RefactoringStatus status= new RefactoringStatus();
		observer.getLock().lock();		
		pm.beginTask("Checking postconditions...", 2);
		IStrategoTerm builderResult = null;
		IStrategoTerm astChanges = null;
		IStrategoTerm textReplaceTerm = null;
		IStrategoTerm fatalErrors = null;
		IStrategoTerm errors = null;
		IStrategoTerm warnings = null;
		try {
			builderResult = getBuilderResult();
			if(builderResult == null){
				observer.reportRewritingFailed();
				String errorMessage = "Refactoring application failed: '" + caption + "'";
				Environment.logException(errorMessage);
				return RefactoringStatus.createFatalErrorStatus(errorMessage);					
			}	
			if (!isValidResultTerm(builderResult)) {
				String errorMessage = "Illegal refactoring result. Expected: '([(original-node, newnode), ... ], fatal-errors, errors, warnings)'";
				Environment.logException(errorMessage);
				return RefactoringStatus.createFatalErrorStatus(errorMessage);
			}
			astChanges = builderResult.getSubterm(0);
			fatalErrors = builderResult.getSubterm(1);
			errors = builderResult.getSubterm(2);
			warnings = builderResult.getSubterm(3);
			updateStatus(status, fatalErrors, RefactoringStatus.FATAL);
			updateStatus(status, errors, RefactoringStatus.ERROR);
			updateStatus(status, warnings, RefactoringStatus.WARNING);
			if(status.hasFatalError())
				return status; //no need to calculate text changes
			textReplaceTerm = getTextReplacement(astChanges);
			if (textReplaceTerm == null) {
				observer.reportRewritingFailed();
				String errorMessage = "Text-reconstruction unexpectedly fails, did you specify a valid pp-table?: \n"+ observer.getLog();
				Environment.logException(errorMessage);
				return RefactoringStatus.createFatalErrorStatus(errorMessage);
			}
			assert(textReplaceTerm.getSubtermCount() == astChanges.getSubtermCount());
			for (int i = 0; i < astChanges.getSubtermCount(); i++) {
				TextFileChange fChange = createTextChange(termAt(astChanges.getSubterm(i),0), textReplaceTerm.getSubterm(i));	
				fileChanges.add(fChange);
			}
		} finally { 
			observer.getLock().unlock();
			pm.done();
		}
		return status;
	}

	private void updateStatus(RefactoringStatus status, IStrategoTerm errors, int severity) {
		for (int i = 0; i < errors.getSubtermCount(); i++) {
			IStrategoTerm error = errors.getSubterm(i);
			String message = formatErrorMessage(error);
			switch (severity) {
				case RefactoringStatus.WARNING:
					status.merge(RefactoringStatus.createWarningStatus(message));		
					break;
				case RefactoringStatus.ERROR:
					status.merge(RefactoringStatus.createErrorStatus(message));		
					break;
				case RefactoringStatus.FATAL:
					status.merge(RefactoringStatus.createFatalErrorStatus(message));		
					break;
				default:
					assert(false);
					break;
			}			
		}
	}

	private String formatErrorMessage(IStrategoTerm error) {
		if(Tools.isTermString(error))
			return Tools.asJavaString(error);
		String message = Tools.asJavaString(error.getSubterm(1));
		if(hasImploderOrigin(error.getSubterm(0))){
			IStrategoTerm origin = OriginAttachment.getOrigin(error.getSubterm(0));
			int line = getLeftToken(origin).getLine();
			int column = getLeftToken(origin).getColumn();
			String fileName = SourceAttachment.getResource(origin).getName(); //project-relative-path?
			return fileName +" ("+ line +"," + column +"): "+ message;
		}
		return message;
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("Creating change...", 1);
			CompositeChange change= new CompositeChange(getName(), fileChanges.toArray(new Change[fileChanges.size()])); 			
			return change;
		} finally {
			monitor.done();
		}
	}
	
	private IStrategoTerm getSelectionNode(EditorState editor) {
		IStrategoTerm node = editor.getSelectionAst(!cursor);
		if (node == null) node = editor.getParseController().getCurrentAst();
		try {
			node = InputTermBuilder.getMatchingNode(semanticNodes, node, false);
		} catch (BadDescriptorException e) {
			Environment.logException("Failed to get selection", e);
		}
		return node;
	}
	
	private IStrategoTerm getBuilderResult() {
		IStrategoTerm userInputTerm = mkInputTerm();
		IStrategoTerm inputTerm = observer.getInputBuilder().makeInputTermRefactoring(userInputTerm, node, true, source);
		
		//DesugaredOriginAttachment.setAllTermsAsDesugaredOrigins(inputTerm.getSubterm(3));
		
		IStrategoTerm result = null;
		try {
			result = observer.invoke(builderRule, inputTerm, getResource());
		} catch (InterpreterErrorExit e) {
			Environment.logException("Builder failed", e);
			e.printStackTrace();
		} catch (UndefinedStrategyException e) {
			Environment.logException("Builder failed", e);
			e.printStackTrace();
		} catch (InterpreterExit e) {
			Environment.logException("Builder failed", e);
			e.printStackTrace();
		} catch (InterpreterException e) {
			Environment.logException("Builder failed", e);
			e.printStackTrace();
		}
		return result;
	}

	private IStrategoTerm mkInputTerm() {
		IStrategoTerm[] inputTerms = new IStrategoTerm[inputFields.size()];
		for (int i = 0; i < inputTerms.length; i++) {
			inputTerms[i] = inputFields.get(i).getInputValue();
		}
		ITermFactory factory = Environment.getTermFactory();
		try {
			IStrategoTuple inputTuple = factory.makeTuple(inputTerms, TermFactory.EMPTY_LIST);
			return inputTuple;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch (AssertionError err) {
			err.printStackTrace();
			return null;
		}
	}

	private IResource getResource() {
		return SourceAttachment.getResource(node);
	}
	
	private boolean isValidResultTerm(IStrategoTerm resultTerm) {
		for (int i = 1; i < resultTerm.getSubtermCount(); i++) {
			if(!isErrorTermList(resultTerm.getSubterm(i)))
				return false;
		}
		return isTermTuple(resultTerm) 
			&& resultTerm.getSubtermCount() == 4 
			&& isValidAstChangeList(resultTerm.getSubterm(0));
	}

	private boolean isErrorTermList(IStrategoTerm errorMessages) {
		for (int i = 0; i < errorMessages.getSubtermCount(); i++) {
			if(!isErrorTerm(errorMessages.getSubterm(i)))
				return false;
		}
		return Tools.isTermList(errorMessages);
	}

	private boolean isErrorTerm(IStrategoTerm errorMessage) {
		return Tools.isTermString(errorMessage) 
		|| (isTermTuple(errorMessage) && errorMessage.getSubtermCount() == 2);
	}

	private boolean isValidAstChangeList(IStrategoTerm astChanges) {
		for (int i = 0; i < astChanges.getSubtermCount(); i++) {
			if(!isTupleWithOrigin(astChanges.getSubterm(i))){
				return false;
			}
		}
		return isTermList(astChanges);
	}

	private boolean isTupleWithOrigin(IStrategoTerm resultTerm) {
		return 
			isTermTuple(resultTerm) && 
			resultTerm.getSubtermCount()==2 &&
			hasImploderOrigin(termAt(resultTerm, 0));
	}
	
	private IStrategoTerm getTextReplacement(IStrategoTerm resultTuple) {
		IStrategoTerm textreplace=construct_textual_change_1_1.instance.invoke(
				observer.getRuntime().getCompiledContext(), 
				resultTuple, 
				new Strategy() {
					@Override
					public IStrategoTerm invoke(Context context, IStrategoTerm current) {
						if (ppStrategy!=null)
							return observer.invokeSilent(ppStrategy, current, getResource());
						return null;
					}
				}
			);
		return textreplace;
	}
	
	private TextFileChange createTextChange(IStrategoTerm originalTerm, IStrategoTerm textReplaceTerm) {
		final int startLocation=Tools.asJavaInt(termAt(textReplaceTerm, 0));
		final int endLocation=Tools.asJavaInt(termAt(textReplaceTerm, 1));
		final String resultText = Tools.asJavaString(termAt(textReplaceTerm, 2));
		final IStrategoTerm originTerm = OriginAttachment.tryGetOrigin(originalTerm);
		final IFile file = (IFile)SourceAttachment.getResource(originTerm);

		TextFileChange textChange = new TextFileChange("", file); 
		textChange.setTextType(file.getFileExtension());
		MultiTextEdit edit= new MultiTextEdit();
		edit.addChild(new ReplaceEdit(startLocation, endLocation - startLocation, resultText));
		textChange.setEdit(edit);
		return textChange;
	}
}
