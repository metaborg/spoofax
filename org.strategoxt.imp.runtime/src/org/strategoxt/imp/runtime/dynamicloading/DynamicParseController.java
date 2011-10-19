package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserverPartListener;
import org.strategoxt.imp.runtime.services.TokenColorerHelper;

/**
 * Dynamic proxy class to a parse controller.
 * 
 * @see AbstractService
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseController extends AbstractService<IParseController> implements IParseController {
	
	public static final int REINIT_PARSE_DELAY = 100;
	
	private static String TESTING_LANGUAGE_NAME = "Spoofax-Testing";
	
	private EditorState lastEditor;
	
	private IPath filePath;
	
	private ISourceProject project;
	
	private IMessageHandler handler;
	
	private boolean isReinitialized;
	
	public DynamicParseController() {
		super(IParseController.class);
	}
	
	/**
	 * Find the language associated with this parse controller,
	 * in case this is not statically known.
	 */
	private Language findLanguage(IPath filePath) {
		// Best to not set lastEditor here; only in getWrapped()
		EditorState editor = lastEditor;
		if (editor == null && EditorState.isUIThread())
			editor = EditorState.getEditorFor(this);
		if (editor != null)
			return editor.getLanguage();
		
		// No active editor; try the registry instead
		return LanguageRegistry.findLanguage(filePath, null);
	}
	
	@Override
	public IParseController getWrapped() {
		// Reinitalize path etc. if descriptor was reloaded
		if (super.getWrapped().getProject() == null)
			initialize(null, null, null);
		
		IParseController result = super.getWrapped(); 

		if (lastEditor == null && EditorState.isUIThread()) {
			lastEditor = EditorState.getEditorFor(this);
			if (lastEditor != null)
				initializeEagerServices(result);
		}
		return result;
	}

	private void initializeEagerServices(IParseController parser) {
		assert lastEditor != null;
		if (parser instanceof SGLRParseController)
			((SGLRParseController) parser).setEditor(lastEditor);
		Descriptor descriptor = Environment.getDescriptor(getLanguage());
		ContentProposerFactory.eagerInit(descriptor, parser, lastEditor);
		AutoEditStrategyFactory.eagerInit(descriptor, parser, lastEditor);
		OnSaveServiceFactory.eagerInit(descriptor, parser, lastEditor);
		TokenColorerHelper.register((SGLRParseController) parser, lastEditor);
		StrategoObserverPartListener.register(lastEditor);
		RefactoringFactory.eagerInit(descriptor, parser, lastEditor);
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return getWrapped().getAnnotationTypeInfo();
	}

	public Object getCurrentAst() {
		return getWrapped().getCurrentAst();
	}

	public ISourcePositionLocator getSourcePositionLocator() {
		return getWrapped().getSourcePositionLocator();
	}

	public IPath getPath() {
		return getWrapped().getPath();
	}

	public ISourceProject getProject() {
		return getWrapped().getProject();
	}
	
	/**
	 * Gets the editor for this parse controller, if it was already known.
	 * 
	 * @see EditorState#getEditorFor(IParseController) to get the actual editor
	 */
	public EditorState getLastEditor() {
		// (must be a simple accessor: used in EditorState.getEditorFor()
		return lastEditor;
	}

	public ILanguageSyntaxProperties getSyntaxProperties() {
		return getWrapped().getSyntaxProperties();
	}

	public Iterator getTokenIterator(IRegion region) {
		return getWrapped().getTokenIterator(region);
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		if (!isInitialized()) {
			Language language = findLanguage(filePath);
			
			initialize(null, language);
		}
		
		// (Re)store these inputs in case the parse controller has been dynamically reloaded
		if (filePath == null) {
			filePath = this.filePath;
			project = this.project; // might be null if not an ISourceProject
			handler = this.handler;
		} else {
			this.filePath = filePath;
			this.project = project;
			this.handler = handler;
		}
		
		super.getWrapped().initialize(filePath, project, handler);
	}
	
	@Override
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		if (isInitialized() && getLastEditor() != null)
			TokenColorerHelper.unregister(getLastEditor());
		
		if (!isTestingFragmentDescriptor(newDescriptor))
			super.reinitialize(newDescriptor);
		isReinitialized = true;
		if (lastEditor != null) {
			initializeEagerServices(getWrapped());
			lastEditor.scheduleParserUpdate(REINIT_PARSE_DELAY);
		}
	}

	/**
	 * Tests if the descriptor to reinitialize is
	 * in fact a descriptor for a fragment language
	 * in the testing language.
	 */
	private boolean isTestingFragmentDescriptor(Descriptor newDescriptor) {
		try {
			return getLanguage() != null
				&& getLanguage().getName().equals(TESTING_LANGUAGE_NAME)
				&& !newDescriptor.getLanguage().getName().equals(TESTING_LANGUAGE_NAME);
		} catch (BadDescriptorException e) {
			return false;
		} catch (RuntimeException e) {
			Environment.logWarning("Unexpected exception", e);
			return false;
		}
	}

	public Object parse(String input, IProgressMonitor monitor) {
		IParseController parser = getWrapped();
		
		Object result = parser.parse(input, monitor);
		if (isReinitialized && lastEditor != null) {
			// Update other services
			lastEditor.getEditor().updateColoring(new Region(0, input.length()));
			// UNDONE: Also called from ParserScheduler.run() 
			// lastEditor.getEditor().fParserScheduler.notifyModelListeners(new NullProgressMonitor());
			isReinitialized = false;
		}
		return result;
	}

	public IResource getResource() {
    	IPath path = getPath();
		IProject project = getProject().getRawProject();
		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
		return project.getFile(path);
	}
}
