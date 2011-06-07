package org.strategoxt.imp.runtime.dynamicloading;


import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.IRefactoring;
import org.strategoxt.imp.runtime.services.IRefactoringMap;
import org.strategoxt.imp.runtime.services.InputTermBuilder;
import org.strategoxt.imp.runtime.services.RefactoringMap;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoRefactoring;
import org.strategoxt.imp.runtime.services.StrategoRefactoringWizard;

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
		IStrategoAppl ppTableTerm = TermReader.findTerm(d.getDocument(), "PPTable");
		String ppTable=null;
		if (ppTableTerm !=null)
			ppTable=termContents(termAt(ppTableTerm, 0));
		IStrategoAppl ppStrategyTerm = TermReader.findTerm(d.getDocument(), "PrettyPrint");
		String ppStrategy=null;
		if(ppStrategyTerm!=null)
			ppStrategy=termContents(termAt(ppStrategyTerm, 0));		
		IStrategoTerm node = controller.getEditor().getSelectionAst(false);
		for (IStrategoAppl builder : collectTerms(d.getDocument(), "Refactoring")) {
			IStrategoTerm[] semanticNodes = termAt(builder,0).getAllSubterms();
			if(getMatchingSelectionNode(semanticNodes, node) != null){
				String caption = termContents(termAt(builder, 1));
				String strategy = termContents(termAt(builder, 2));
				IStrategoList options = termAt(builder, 3);			
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
							ppTable,
							ppStrategy,
							semanticNodes
						)
					);
				}
			}
		}
		return refactorings;
	}

	private static IStrategoTerm getMatchingSelectionNode(IStrategoTerm[] semanticNodes,
			IStrategoTerm node) throws BadDescriptorException {
		return InputTermBuilder.getMatchingNode(semanticNodes, node, false);
	}

}
