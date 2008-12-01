package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Dynamic proxy class to a parse controller.
 * 
 * @see DynamicService
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseController extends DynamicService<IParseController> implements IParseController {
	private IPath filePath;
	private ISourceProject project;
	private IMessageHandler handler;
	
	public DynamicParseController() {
		super(IParseController.class);
	}
	
	/**
	 * Find the language associated with this parse controller,
	 * in case this is not statically known.
	 */
	private Language findLanguage(IPath filePath) {
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		if (activePage != null) {
			IEditorPart activeEditor = activePage.getActiveEditor();
			
			if (isMyEditor(activeEditor))
				return ((UniversalEditor) activeEditor).fLanguage;
			
			// Search for an active editor with this parser
			for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				for (IWorkbenchPage page : window.getPages()) {
					for (IEditorReference reference : page.getEditorReferences()) {
						IWorkbenchPart editor = reference.getPart(false);
						if (isMyEditor(editor))
							return ((UniversalEditor) editor).fLanguage;
					}
				}
			}
		}
		
		// No active editor; try the registry instead
		// TODO: Use language validator?
		return LanguageRegistry.findLanguage(filePath, null);
	}
	
	private boolean isMyEditor(IWorkbenchPart editor) {
		if (editor instanceof UniversalEditor) {
			IParseController controller = ((UniversalEditor) editor).getParseController();
			if (controller == this)
				return true;
		}
		return false;
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return getWrapped().getAnnotationTypeInfo();
	}

	public Object getCurrentAst() {
		return getWrapped().getCurrentAst();
	}

	public ISourcePositionLocator getNodeLocator() {
		return getWrapped().getNodeLocator();
	}

	public IPath getPath() {
		return getWrapped().getPath();
	}

	public ISourceProject getProject() {
		return getWrapped().getProject();
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
			
			initialize(language);
		}
		
		// (Re)store these inputs in case the parse controller has been dynamically reloaded
		if (filePath == null) filePath = this.filePath;
		else this.filePath = filePath;
		if (filePath == null) project = this.project;
		else this.project = project;
		if (filePath == null) handler = this.handler;
		else this.handler = handler;
		
		getWrapped().initialize(filePath, project, handler);
	}

	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		return getWrapped().parse(input, scanOnly, monitor);
	}
}
