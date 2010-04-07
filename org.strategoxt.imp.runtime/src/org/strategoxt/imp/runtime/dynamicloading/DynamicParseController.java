package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
			if (lastEditor != null) {
				Descriptor descriptor = Environment.getDescriptor(getLanguage());
				ContentProposerFactory.eagerInit(descriptor, result, lastEditor);
				AutoEditStrategyFactory.eagerInit(descriptor, result, lastEditor);
				OnSaveServiceFactory.eagerInit(descriptor, result, lastEditor);
				TokenColorerHelper.register(lastEditor, (SGLRParseController) result);
			}
		}
		return result;
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
		if (filePath == null) filePath = this.filePath;
		else this.filePath = filePath;
		if (project == null) project = this.project;
		else this.project = project;
		if (handler == null) handler = this.handler;
		else this.handler = handler;
		
		super.getWrapped().initialize(filePath, project, handler);
	}
	
	@Override
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		if (isInitialized() && getLastEditor() != null)
			TokenColorerHelper.unregister(getLastEditor());
		super.reinitialize(newDescriptor);
		isReinitialized = true;
		if (lastEditor != null) {
			lastEditor.scheduleParserUpdate(REINIT_PARSE_DELAY);
			Descriptor descriptor = Environment.getDescriptor(getLanguage());
			ContentProposerFactory.eagerInit(descriptor, getWrapped(), lastEditor);
			AutoEditStrategyFactory.eagerInit(descriptor, getWrapped(), lastEditor);
			OnSaveServiceFactory.eagerInit(descriptor, getWrapped(), lastEditor);
		}
	}

	public Object parse(String input, IProgressMonitor monitor) {
		IParseController parser = getWrapped();
		if (parser instanceof SGLRParseController)
			((SGLRParseController) parser).setEditor(lastEditor);
		
		Object result = parser.parse(input, monitor);
		if (isReinitialized && lastEditor != null) {
			// Update other services
			lastEditor.getEditor().updateColoring(new Region(0, input.length()));
			lastEditor.getEditor().fParserScheduler.notifyModelListeners(new NullProgressMonitor());
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
