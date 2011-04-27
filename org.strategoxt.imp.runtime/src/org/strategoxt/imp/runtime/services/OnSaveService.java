/**
 * 
 */
package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.IOnSaveService;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.RefreshResourcePrimitive;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OnSaveService implements IOnSaveService {

	private final StrategoObserver runtime;
	
	private final String function;
	
	private EditorState editor;
	
	public OnSaveService(StrategoObserver runtime, String function) {
		this.runtime = runtime;
		this.function = function;
	}
	
	public void initialize(EditorState editor) {
		this.editor = editor;
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		// Unused
	}

	public void documentChanged(DocumentEvent event) {
		if (function == null) return;
		
		//String contents = event.getDocument().get();
		
		try {
			Environment.getStrategoLock().lock();
			try {
				IStrategoTerm ast = editor.getCurrentAst();
				if (ast == null) return;
				
				IStrategoTerm result = runtime.invokeSilent(function, ast);
				if (result == null) {
					runtime.reportRewritingFailed();
					String log = runtime.getLog();
					Environment.logException(log.length() == 0 ? "Analysis failed" : "Analysis failed:\n" + log);
					AstMessageHandler messages = runtime.getMessages();
					messages.clearMarkers(SourceAttachment.getResource(ast));
					messages.addMarkerFirstLine(SourceAttachment.getResource(ast), "Analysis failed (see error log)", IMarker.SEVERITY_ERROR);
					messages.commitAllChanges();
				} else if (isTermString(result)) {
					// Function's returning a filename
					String file = asJavaString(termAt(result, 0));
					if (new File(file).exists())
						RefreshResourcePrimitive.call(runtime.getRuntime().getContext(), file);	
				} else if (isTermTuple(result) && result.getSubtermCount() == 2 && isTermString(termAt(result, 0)) && isTermString(termAt(result, 1))) {
					// Function's returning a tuple like a builder
					// let's be friendly and try to refresh the file
					String file = asJavaString(termAt(result, 0));
					String newContents = asJavaString(termAt(result, 1));
					try {
						IFile resource = EditorIOAgent.getFile(runtime.getRuntime().getContext(), file);
						StrategoBuilder.setFileContentsDirect(resource, newContents);
					} catch (FileNotFoundException e) {
						Environment.logException("Problem when handling on save event", e);
					} catch (CoreException e) {
						Environment.logException("Problem when handling on save event", e);
					}
				} else if (!"None".equals(cons(result))) {
					if (editor.getDescriptor().isDynamicallyLoaded())
						Environment.logWarning("Unexpected result from 'on save' strategy: should be None() or (\"filename\", \"contents\"): " + result);
				}
			} finally {
				Environment.getStrategoLock().unlock();
			}
		} catch (RuntimeException e) {
			Environment.logException("Exception in OnSaveService", e);
		} catch (Error e) {
			Environment.logException("Exception in OnSaveService", e);
		}
	}

}
