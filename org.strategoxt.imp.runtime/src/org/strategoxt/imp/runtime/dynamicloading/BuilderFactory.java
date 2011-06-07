package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.imp.editor.UniversalEditor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.BuilderMap;
import org.strategoxt.imp.runtime.services.CustomStrategyBuilder;
import org.strategoxt.imp.runtime.services.DebugModeBuilder;
import org.strategoxt.imp.runtime.services.IBuilder;
import org.strategoxt.imp.runtime.services.IBuilderMap;
import org.strategoxt.imp.runtime.services.StrategoBuilder;
import org.strategoxt.imp.runtime.services.StrategoBuilderListener;
import org.strategoxt.imp.runtime.services.StrategoObserver;

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
		if (Environment.allowsDebugging(d)) // Descriptor allows debugging)
		{
			addDebugModeBuilder(d, controller, builders, derivedFromEditor);
		}
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

	/**
	 * Adds a Debug Mode Builder, if debug mode is allowed the user can choose to enable stratego debugging.
	 * If debugging is enabled, a new JVM is started for every strategy invoke resulting in major performance drops.
	 * The user can also disable Debug mode, without needing to rebuil the project. 
	 */
	private static void addDebugModeBuilder(Descriptor d, SGLRParseController controller,
			Set<IBuilder> builders, EditorState derivedFromEditor) throws BadDescriptorException
	{
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		builders.add(new DebugModeBuilder(feedback));
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
