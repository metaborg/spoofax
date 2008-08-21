package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;
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
import org.spoofax.jsglr.InvalidParseTableException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * This class functions as a dynamic proxy to a parse controller.
 * All dynamically loaded languages inherit from the "Root" language, which
 * specifies this class as its parse controller, allowing it to be
 * adapted at run time.  
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseController implements IParseController {
	private SGLRParseController wrapped;
	
	private Throwable notLoadingCause;
	
	private Language language;
	
	public Language getLanguage() {
		if (language != null) return language;
		if (getWrapped() != null) return getWrapped().getLanguage();
		
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editor : page.getEditorReferences()) {
					IWorkbenchPart part = editor.getPart(false);
					if (part instanceof UniversalEditor) {
						IParseController controller = ((UniversalEditor) part).getParseController();
						if (controller == this) {
							language = ((UniversalEditor) part).fLanguage;
							return language;
						}
					}
				}
			}
		}
		
		throw new IllegalStateException("Cannot reference the parse controller language at this time");
	}
	
	public IParseController getWrapped() {
		if (wrapped == null) {
			if (notLoadingCause != null) {
				if (notLoadingCause instanceof RuntimeException) throw (RuntimeException) notLoadingCause;
				else throw new RuntimeException(notLoadingCause);
			} else {
				Language language = getLanguage(); // or LanguageRegistry.findLanguage(filePath, null);
				Environment.getParseTable(language);
				wrapped = new SGLRParseController(language, Environment.getDescriptor(language).getStartSymbol());
			}
		}
		return wrapped;
	}
	
	public void load(InputStream descriptor, InputStream parseTable) {
		try {
			language = LanguageLoader.load(descriptor, false);
			Environment.registerParseTable(language, parseTable);
			wrapped = null;
		} catch (BadDescriptorException e) {
			Environment.logException("Error in editor service descriptor", e);
			notLoadingCause = e;
		} catch (IOException e) {
			Environment.logException("Could not read editor service descriptor", e);
			notLoadingCause = e;
		} catch (InvalidParseTableException e) {
			Environment.logException("Could not load editor service parse table", e);
			notLoadingCause = e;
		} catch (Exception e) {
			Environment.logException("Could not load editor service descriptor", e);
			notLoadingCause = e;
		}
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
