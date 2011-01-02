package org.strategoxt.imp.runtime.services;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.TargetLink;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Handles the "go to definition" command.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ReferenceResolverDelegate extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final EditorState editor = EditorState.getActiveEditor();
		if (editor != null) {
			// Can't run this in the main thread since we're acquiring the environment lock
			final ISimpleTerm reference = editor.getSelectionAst(false);
			new Job("Go to definition") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					followLink(editor, reference);
					return Status.OK_STATUS;
				}
				
			}.schedule();
		}
		return null;
	}

	private void followLink(EditorState editor, ISimpleTerm reference) {
		try {
			SGLRParseController controller = editor.getParseController();
			ISourcePositionLocator locator = controller.getSourcePositionLocator();
			
			IReferenceResolver resolver = editor.getDescriptor().createService(IReferenceResolver.class, controller);
			Object target = resolver.getLinkTarget(reference, controller);
			if (target == null)
				return;
			
			int targetStart = locator.getStartOffset(target) < 0 ? 0 : locator.getStartOffset(target);
			int targetLength = 0; // targetStart == 0 ? 0 : locator.getEndOffset(target) - targetStart + 1;
			IPath path = locator.getPath(target);
			
			openLink(new TargetLink(reference.toString(), 0, 0, path, targetStart, targetLength, null));
			
		} catch (BadDescriptorException e) {
			Environment.logException("Failed to load reference resolver", e);
		}
	}

	private void openLink(final TargetLink targetLink) {
		Job job = new UIJob("Select definition") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				targetLink.open();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

}
