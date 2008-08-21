package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This class functions as a dynamic proxy to a parse controller.
 * All dynamically loaded languages inherit from the "Root" language, which
 * specifies this class as its parse controller, allowing it to be
 * adapted at run time.  
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseController extends DynamicService<IParseController> implements IParseController {
	public DynamicParseController() {
		super(IParseController.class);
	}
	
	@Override
	protected IParseController getWrapped() {
		if (getLanguage() == null) init(findLanguage());
		init(getLanguage());
		return super.getWrapped();
	}
	
	/**
	 * Find the language associated with this parse controller,
	 * in case this is not statically known.
	 */
	private Language findLanguage() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editor : page.getEditorReferences()) {
					IWorkbenchPart part = editor.getPart(false);
					if (part instanceof UniversalEditor) {
						IParseController controller = ((UniversalEditor) part).getParseController();
						if (controller == this) {
							return ((UniversalEditor) part).fLanguage;
						}
					}
				}
			}
		}
		
		throw new IllegalStateException("Cannot reference the parse controller language at this time");
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
		getWrapped().initialize(filePath, project, handler);
	}

	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		return getWrapped().parse(input, scanOnly, monitor);
	}
}
