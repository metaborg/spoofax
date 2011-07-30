package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.jsglr.origin.AbstractOriginPrimitive;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;

/**
 * Checks if a term falls (partially) within the area selected by the user
 */
public class InSelectedFragmentPrimitive extends AbstractOriginPrimitive {
	
	public InSelectedFragmentPrimitive() {
		super("SSL_EXT_in_selected_fragment");
	}

	@Override
	public IStrategoTerm call(IContext env, IStrategoTerm origin) {
		final int startOffset = ImploderAttachment.getLeftToken(origin).getStartOffset();
		final int endOffset = ImploderAttachment.getRightToken(origin).getEndOffset();
		final boolean[] inSelection = new boolean[]{true};
		Job job = new UIJob("in selected fragment? primitive") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Point selected = EditorState.getActiveEditor().getEditor().getSelection();
				if(selected.x > endOffset)
					inSelection[0] = false;
				if(selected.x + selected.y -1 < startOffset)
					inSelection[0] = false;
				if(selected.y == 0)
					inSelection[0] = false;
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			Environment.logException(e);
		}
		if (job.getResult()==Status.OK_STATUS && inSelection[0]){
			return origin;
		}
		return null;
	}
}
