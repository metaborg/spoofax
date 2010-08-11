package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermList;
import static org.spoofax.interpreter.core.Tools.isTermString;

import org.eclipse.jface.dialogs.InputDialog;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

//example: div = ?(x,y); (prim("SSL_divi",x,y) <+ prim("SSL_divr",x,y))
//example: opendialog = prim("SSL_EXT_opendialog",id,"bla") 

/**
 * @author Maartje de Jonge
 */
public class DialogPrimitive extends AbstractPrimitive {

	public DialogPrimitive() {
		super("SSL_EXT_opendialog", 0, 3); //geef strategien mee (1) en termen (3 -> title, old-name,--)
	}
	
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		//test input
		//if (!is..(svars[0])) return false;
		if (!isTermString(tvars[0]) && !isTermList(tvars[0])) return false;
		if (!isTermString(tvars[1])) return false;
		if (!isTermString(tvars[2])) return false;
		
		//geef goede namen
		IStrategoString title = (IStrategoString)tvars[0]; 
		IStrategoString message = (IStrategoString)tvars[1];
		IStrategoString input = (IStrategoString)tvars[2]; 
		
		
		Environment.getStrategoLock().lock();
		try {
			InputDialog dialog = new InputDialog(null, title.stringValue(), message.stringValue(), input.stringValue(), null);
			if (dialog.open() == InputDialog.OK) {
				String userInput=dialog.getValue();		
				env.setCurrent(env.getFactory().makeString(userInput));
				return true;
			} 
		} finally {
			Environment.getStrategoLock().unlock();
		}
		return false;
		/*
		Environment.getStrategoLock().lock();
		try {
			//open dialog
			Display display = new Display();
		    final Shell shell = new Shell(display);
			RenameDialog dialog=new RenameDialog(shell);
			dialog.setText(title.stringValue());
			dialog.setMessage(message.stringValue());
			dialog.setInput(input.stringValue());
			String userInput=dialog.open();		
			env.setCurrent(env.getFactory().makeString(userInput));		
		}*/		
	}

	

	

}
