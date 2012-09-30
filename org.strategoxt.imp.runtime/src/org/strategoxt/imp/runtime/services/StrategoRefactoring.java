package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

public class StrategoRefactoring extends Refactoring implements IRefactoring {
		
	//refactoring specific fields
	private final String actionDefinitionId; //Keybinding

	private final String caption; 
	
	private final String builderRule;
			
	private final boolean cursor;
	
	private final boolean source;
	
	private final IStrategoTerm[] semanticNodes;
			
	protected final ArrayList<StrategoRefactoringIdentifierInput> inputFields;
	
	
	// language specific
	private final StrategoTextChangeCalculator textChangeCalculator;

	
	// selected node when refactoring is triggered, calculated from user selection
	private IStrategoTerm node;

	
	// results
	private ArrayList<IPath> affectedFilePaths;

	private Collection<TextFileChange> fileChanges;


	// others
	private final StrategoObserver observer;

	private IAction action;
	

	
	public boolean isDefinedOnSelection(EditorState editor) {
		return getSelectionNode(editor) != null;
	}

	public String getCaption() {
		return caption;
	}

	public String getActionDefinitionId() {
		// TODO 
		return actionDefinitionId;
	}

	public ArrayList<StrategoRefactoringIdentifierInput> getInputFields() {
		return inputFields;
	}
	
	public void setAction(IAction action) {
		this.action = action;
	}

	public IAction getAction() {
		assert(action != null) : "refactoring action is not set";
		return action;
	}

	public void prepareExecute(EditorState editor) {
		this.node = getSelectionNode(editor);
		this.fileChanges.clear();
		this.affectedFilePaths.clear();
		//inputFields.clear(); set default values?
	}

	public StrategoRefactoring(StrategoObserver observer, String caption, String builderRule,
			boolean cursor, boolean source, StrategoTextChangeCalculator textChangeCalculator,
			IStrategoTerm[] semanticNodes, ArrayList<StrategoRefactoringIdentifierInput> inputFields, String actionDefinitionId) {
		this.cursor=cursor;
		this.source=source;
		this.observer = observer;
		this.caption = caption;
		this.builderRule = builderRule;
		this.semanticNodes = semanticNodes;
		this.textChangeCalculator = textChangeCalculator;
		this.fileChanges = new HashSet<TextFileChange>();
		this.inputFields = inputFields;
		this.affectedFilePaths = new ArrayList<IPath>();
		this.actionDefinitionId = actionDefinitionId;
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
			fillAffectedFilePaths(astChanges);
			//if(1<2) return RefactoringStatus.createFatalErrorStatus("TEST");
			fatalErrors = builderResult.getSubterm(1);
			errors = builderResult.getSubterm(2);
			warnings = builderResult.getSubterm(3);
			updateStatus(status, fatalErrors, RefactoringStatus.FATAL);
			updateStatus(status, errors, RefactoringStatus.ERROR);
			updateStatus(status, warnings, RefactoringStatus.WARNING);
			if(status.hasFatalError())
				return status; //no need to calculate text changes
			fileChanges = textChangeCalculator.getFileChanges(astChanges, observer);
			if (fileChanges == null) {
				observer.reportRewritingFailed();
				String errorMessage = "Text-reconstruction unexpectedly fails, did you specify a suitable pretty-print strategy?: \n"+ observer.getLog();
				Environment.logException(errorMessage);
				return RefactoringStatus.createFatalErrorStatus(errorMessage);
			}
		} finally { 
			observer.getLock().unlock();
			pm.done();
		}
		return status;
	}

	private void updateStatus(RefactoringStatus status, IStrategoTerm errors, int severity) {
		for (int i = 0; i < errors.getSubtermCount(); i++) {
			final IStrategoTerm error = errors.getSubterm(i);
			String message = formatErrorMessage(error);
			RefactoringStatusContext context = getRefactoringStatusContext(error);
			switch (severity) {
				case RefactoringStatus.WARNING:
					status.merge(RefactoringStatus.createWarningStatus(message, context));		
					break;
				case RefactoringStatus.ERROR:
					status.merge(RefactoringStatus.createErrorStatus(message, context));		
					break;
				case RefactoringStatus.FATAL:
					status.merge(RefactoringStatus.createFatalErrorStatus(message, context));		
					break;
				default:
					assert(false);
					break;
			}			
		}
	}

	private RefactoringStatusContext getRefactoringStatusContext(final IStrategoTerm error) {
		RefactoringStatusContext context =  null;
		if(hasImploderOrigin(error.getSubterm(0))){
			final IStrategoTerm origin = OriginAttachment.getOrigin(error.getSubterm(0));
			final IFile file = (IFile)SourceAttachment.getResource(origin);
			final int startOffset = ImploderAttachment.getLeftToken(origin).getStartOffset();
			final int endOffset = ImploderAttachment.getRightToken(origin).getEndOffset();
			context = new FileStatusContext(file, new IRegion() {
				public int getOffset() {
					return startOffset;
				}
				
				public int getLength() {
					return Math.max(endOffset - startOffset, 1);
				}
			});
		}
		return context;
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
			CompositeChange change = new CompositeChange(getName(), fileChanges.toArray(new Change[fileChanges.size()])); 			
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
			result = observer.invoke(builderRule, inputTerm, getFile());
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
		if(inputTerms.length == 0) {
			IStrategoConstructor noneCons = factory.makeConstructor("None", 0);
			return factory.makeAppl(noneCons);
		}
		if(inputTerms.length == 1)
			return inputTerms[0];
		IStrategoTuple inputTuple = factory.makeTuple(inputTerms, TermFactory.EMPTY_LIST);
		return inputTuple;
	}

	private File getFile() {
		return SourceAttachment.getFile(node);
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
		
	private void fillAffectedFilePaths(IStrategoTerm astChanges) {
		affectedFilePaths.clear();
		for (int i = 0; i < astChanges.getSubtermCount(); i++) {
			IStrategoTerm affectedTerm = termAt(astChanges.getSubterm(i),0);
			IStrategoTerm affectedOrigin = OriginAttachment.tryGetOrigin(affectedTerm);
			IResource file = SourceAttachment.getResource(affectedOrigin);
			assert(file != null) : "File of affected term is unknown";
			IPath path = file.getProjectRelativePath();
			affectedFilePaths.add(path);
		}
	}

	public ArrayList<IPath> getRelativePathsOfAffectedFiles() {
		return affectedFilePaths;
	}
	
}
