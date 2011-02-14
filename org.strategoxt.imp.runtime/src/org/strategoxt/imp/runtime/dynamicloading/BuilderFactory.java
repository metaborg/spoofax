package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.imp.editor.UniversalEditor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.BuilderMap;
import org.strategoxt.imp.runtime.services.CustomStrategyBuilder;
import org.strategoxt.imp.runtime.services.IBuilder;
import org.strategoxt.imp.runtime.services.IBuilderMap;
import org.strategoxt.imp.runtime.services.NodeMapping;
import org.strategoxt.imp.runtime.services.StrategoBuilder;
import org.strategoxt.imp.runtime.services.StrategoBuilderListener;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoRefactoring;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BuilderFactory extends AbstractServiceFactory<IBuilderMap> {
	
	public BuilderFactory() {
		super(IBuilderMap.class, false); // not cached; depends on derived editor relation
	}

	@Override
	public IBuilderMap create(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		Set<IBuilder> builders = new LinkedHashSet<IBuilder>();
		
		EditorState derivedFromEditor = getDerivedFromEditor(controller);
		
		if (d.isATermEditor() && derivedFromEditor != null)
			addDerivedBuilders(derivedFromEditor, builders);

		addBuilders(d, controller, builders, null);
		addCustomStrategyBuilder(d, controller, builders, derivedFromEditor);
		if (EditorState.isUIThread()) // don't show for background (realtime) builders; not threadsafe
			addRefactorings(d, controller, builders);
		return new BuilderMap(builders);
	}

	private static void addBuilders(Descriptor d, SGLRParseController controller, Set<IBuilder> builders,
			EditorState derivedFromEditor) throws BadDescriptorException {
		
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		
		for (IStrategoAppl builder : collectTerms(d.getDocument(), "Builder")) {
			String caption = termContents(termAt(builder, 0));
			String strategy = termContents(termAt(builder, 1));
			IStrategoList options = termAt(builder, 2);
			
			boolean openEditor = false;
			boolean realTime = false;
			boolean persistent = false;
			boolean meta = false;
			boolean cursor = false;
			boolean source = false;
			
			for (IStrategoTerm option : options.getAllSubterms()) {
				String type = cons(option);
				if (type.equals("OpenEditor")) {
					openEditor = true;
				} else if (type.equals("RealTime")) {
					realTime = true;
				} else if (type.equals("Persistent")) {
					persistent = true;
				} else if (type.equals("Meta")) {
					meta = true;
				} else if (type.equals("Cursor")) {
					cursor = true;
				} else if (type.equals("Source")) {
					source = true;
				} else {
					throw new BadDescriptorException("Unknown builder annotation: " + type);
				}
			}
			if (!meta || d.isDynamicallyLoaded())			
				builders.add(new StrategoBuilder(feedback, caption, strategy, openEditor, realTime, cursor, source, persistent, derivedFromEditor));
		}
	}
	
	private static void addRefactorings(Descriptor d, SGLRParseController controller, Set<IBuilder> builders) throws BadDescriptorException {
				
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		
		IStrategoAppl ppTableTerm = TermReader.findTerm(d.getDocument(), "PPTable");
		String ppTable=null;
		if (ppTableTerm !=null)
			ppTable=termContents(termAt(ppTableTerm, 0));
		IStrategoAppl ppStrategyTerm = TermReader.findTerm(d.getDocument(), "PrettyPrint");
		String ppStrategy=null;
		if(ppStrategyTerm!=null)
			ppStrategy=termContents(termAt(ppStrategyTerm, 0));
		
		for (IStrategoAppl builder : collectTerms(d.getDocument(), "Refactoring")) {
			if(isDefinedOnSelection(builder)){
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
					builders.add(
						new StrategoRefactoring(
							feedback, 
							caption, 
							strategy,
							cursor, 
							source, 
							ppTable,
							ppStrategy,
							controller.getResource()
						)
					);
				}
			}
		}
	}

	private static boolean isDefinedOnSelection(IStrategoAppl builder)
			throws BadDescriptorException {
		ArrayList<NodeMapping<String>> mappings=new ArrayList<NodeMapping<String>>();
		for (IStrategoTerm semanticNode : termAt(builder,0).getAllSubterms()) {
			NodeMapping<String> aMapping = NodeMapping.create(semanticNode, "");
			mappings.add(aMapping);
		}
		if(mappings.size()==0){
			return true; //no sort restriction specified
		}
		// XXX: the builder doesn't run in the UI thread for real-time builds
		EditorState editor = EditorState.getActiveEditor();
		IStrategoTerm node= editor.getSelectionAst(false);
		IStrategoTerm ancestor = StrategoTermPath.getMatchingAncestor(node, false);
		IStrategoTerm selectionNode = node;
		boolean isMatch=false;
		do {
			isMatch = NodeMapping.getFirstAttribute(mappings, tryGetName(selectionNode), getSort(selectionNode), 0)!=null;
			selectionNode = getParent(selectionNode);
		} while(!isMatch && selectionNode!=null && selectionNode!=getParent(ancestor));
		//Sublist with single element
		/* XXX: this makes no sense .. taking the constructor of a list?
		if(!isMatch && (!isTermList(ancestor) && isTermList(getParent(ancestor)){
			IStrategoTerm singleElementList= new AstNodeFactory().createSublist((ListAstNode)ancestor.getParent(), ancestor, ancestor, true);
			isMatch= NodeMapping.getFirstAttribute(mappings, singleElementList.getConstructor(), singleElementList.getSort(), 0)!=null;
		}
		*/
		return isMatch;
	}
	
	private static String tryGetName(IStrategoTerm term) {
		IStrategoConstructor cons = tryGetConstructor(term);
		return cons == null ? null : cons.getName();
	}

	private static void addDerivedBuilders(EditorState derivedFromEditor, Set<IBuilder> builders)
			throws BadDescriptorException {		
		if (derivedFromEditor != null){
			addBuilders(derivedFromEditor.getDescriptor(), derivedFromEditor.getParseController(), builders, derivedFromEditor);
		}
	}

	private static void addCustomStrategyBuilder(Descriptor d, SGLRParseController controller,
			Set<IBuilder> builders, EditorState derivedFromEditor) throws BadDescriptorException {
		
		if (d.isATermEditor() && derivedFromEditor != null) {
			StrategoObserver feedback = derivedFromEditor.getDescriptor().createService(StrategoObserver.class, controller);
			builders.add(new CustomStrategyBuilder(feedback, derivedFromEditor));
		} else if (d.isDynamicallyLoaded()) {
			StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
			builders.add(new CustomStrategyBuilder(feedback, null));
		}
	}

	private static EditorState getDerivedFromEditor(SGLRParseController controller) {
		if (controller.getEditor() == null || controller.getEditor().getEditor() == null)
			return null;
		UniversalEditor editor = controller.getEditor().getEditor();
		StrategoBuilderListener listener = StrategoBuilderListener.getListener(editor);
		if (listener == null)
			return null;
		UniversalEditor sourceEditor = listener.getSourceEditor();
		if (sourceEditor == null)
			return null;
		return EditorState.getEditorFor(sourceEditor.getParseController());
	}

}
