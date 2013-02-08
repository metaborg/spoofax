package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverFactory extends AbstractServiceFactory<StrategoObserver> {
	
	public StrategoObserverFactory() {
		super(StrategoObserver.class, true);
	}
	
	@Override
	public StrategoObserver create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		// TODO: Sharing of FeedBack instances??
		//       Each file should have its own Context, I guess, but not its own HybridInterpreter
		IStrategoAppl observer = findTerm(descriptor.getDocument(), "SemanticObserver");
		String observerFunction = null;
		boolean multifile = false;
		
		if(observer != null) {
			observerFunction = termContents(termAt(observer, 0));
			try {
				IStrategoList options = termAt(observer, 1);
				for (IStrategoTerm option : options.getAllSubterms()) {
					String type = cons(option);
					if (type.equals("MultiFile")) {
						multifile = true;
					}
				}
				
			} catch (Exception e) {
				// Ignore exception, multifile stays false.
			}
		}

		return new StrategoObserver(descriptor, observerFunction, multifile);
	}
}