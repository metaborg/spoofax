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
		super(IBuilderMap.class, true);
	}

	@Override
	public IBuilderMap create(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		Set<IBuilder> builders = new LinkedHashSet<IBuilder>();
		
		if (d.getLanguage().getName().equals("ATerm"))
			addSourceBuilders(controller, builders);

		addBuilders(d, controller, builders, false);
		
		return new BuilderMap(builders);
	}

	private static void addBuilders(Descriptor d, SGLRParseController controller, Set<IBuilder> builders,
			boolean operateOnATerms) throws BadDescriptorException {
		
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
				} else {
					throw new BadDescriptorException("Unknown builder annotation: " + type);
				}
			}
			if (!meta || d.isDynamicallyLoaded())			
				builders.add(new StrategoBuilder(feedback, caption, strategy, openEditor, realTime, cursor, persistent, operateOnATerms));
		}
	}

	private static void addSourceBuilders(SGLRParseController controller, Set<IBuilder> builders)
			throws BadDescriptorException {
		
		if (controller.getEditor() == null || controller.getEditor().getEditor() == null)
			return;
		UniversalEditor editor = controller.getEditor().getEditor();
		StrategoBuilderListener listener = StrategoBuilderListener.getListener(editor);
		if (listener == null)
			return;
		UniversalEditor sourceEditor = listener.getSourceEditor();
		if (sourceEditor == null)
			return;
		EditorState sourceEditorState = EditorState.getEditorFor(sourceEditor.getParseController());
		addBuilders(sourceEditorState.getDescriptor(), sourceEditorState.getParseController(), builders, true);
	}

}
