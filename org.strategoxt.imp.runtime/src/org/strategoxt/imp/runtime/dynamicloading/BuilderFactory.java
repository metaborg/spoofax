package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.BuilderMap;
import org.strategoxt.imp.runtime.services.IBuilder;
import org.strategoxt.imp.runtime.services.IBuilderMap;
import org.strategoxt.imp.runtime.services.StrategoBuilder;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BuilderFactory extends AbstractServiceFactory<IBuilderMap> {
	
	public BuilderFactory() {
		super(IBuilderMap.class, true);
	}

	@Override
	public IBuilderMap create(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		Set<IBuilder> builders = new LinkedHashSet<IBuilder>();
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		
		for (IStrategoAppl builder : collectTerms(d.getDocument(), "Builder")) {
			String caption = termContents(termAt(builder, 0));
			String strategy = termContents(termAt(builder, 1));
			IStrategoList options = termAt(builder, 2);
			
			boolean openEditor = false;
			boolean realTime = false;
			boolean persistent = false;
			boolean meta = false;
			
			for (IStrategoTerm option : options.getAllSubterms()) {
				String type = cons(option);
				if (type.equals("OpenEditor")) {
					openEditor = true;
				} else if (type.equals("RealTime")) {
					realTime = true;
				} else if (type.equals("Persistent")) {
					persistent = true;
				} else if (type.equals("Meta")) {
					meta = true;
				} else {
					throw new BadDescriptorException("Unknown builder annotation: " + type);
				}
			}
			if (!meta || d.isDynamicallyLoaded())			
				builders.add(new StrategoBuilder(feedback, caption, strategy, openEditor, realTime, persistent));
		}
		
		return new BuilderMap(builders);
	}

}
