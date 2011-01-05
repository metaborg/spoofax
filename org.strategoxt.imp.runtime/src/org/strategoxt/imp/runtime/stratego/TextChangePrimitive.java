package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoTuple;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;

/**
 * Applies a text-change in the current document
 * Arguments: (IStrategoTerm, (offset, end-offset), "textfragment") 
 * (ASTnode, -1,-1, textfragment) => replace complete file
 * @author Maartje de Jonge
 */
public class TextChangePrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_text_change";

	public TextChangePrimitive() {
		super(NAME, 0, 3);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		int position_start = -1;
		int position_end = -1;
		if (tvars.length!=3)//!isTermString(tvars[1])
			return false;
		if(!(tvars[0] instanceof IStrategoTerm && islocationTuple(tvars[1]) && tvars[2] instanceof IStrategoString))
			return false;
		EditorState editor = ((IStrategoTerm)tvars[0]).getNode().getParseController().getEditor();
		ILexStream lexStream = ((IStrategoTerm)tvars[0]).getNode().getLeftToken().getInput();
		StrategoTuple tuple=(StrategoTuple)tvars[1];
		position_start=((StrategoInt)tuple.get(0)).intValue();
		position_end=((StrategoInt)tuple.get(1)).intValue()-1; //exclusive end pos
		if(position_start< 0 && position_end < 0){
			position_start=0;
			position_end=lexStream.getTokenCount()-1;
		}
		if(DocumentStructure.isUnvalidInterval(position_start, position_end, lexStream))
			return false;
		String text = ((IStrategoString)tvars[2]).stringValue();
		try {
			String newContent = applyTextChange(editor, position_start, position_end, text);
			IStrategoString result = env.getFactory().makeString(newContent);
			env.setCurrent(result);
			return true;
		} 
		catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private String applyTextChange(EditorState editor, final int position_start, final int position_end,
			final String text) throws BadLocationException {
		final IDocument doc = editor.getDocument();
		Job job = new UIJob("apply textchange") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					doc.replace(position_start, position_end+1-position_start, text);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					Environment.logException("Bad location of the replaced fragment", e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		String newContent = doc.get();
		return newContent;
	}

	private boolean islocationTuple(IStrategoTerm term) {
		if (!(term instanceof StrategoTuple))
			return false;
		StrategoTuple tuple=(StrategoTuple)term;
		if(tuple.size()!=2)
			return false;
		for (int i = 0; i < tuple.size(); i++) {
			if(!(tuple.get(i) instanceof StrategoInt))
				return false;
		}
		return true;
	}
}
