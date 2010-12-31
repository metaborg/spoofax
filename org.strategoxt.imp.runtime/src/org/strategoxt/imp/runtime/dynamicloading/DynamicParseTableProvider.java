package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;

/**
 * A parse table provider that can dynamically use different parse tables for different files.
 * 
 * One can exist for each editor.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicParseTableProvider extends ParseTableProvider {
	
	private final String providerFunction;

	private SGLRParseController controller;
	
	private StrategoObserver runtime;
	
	public DynamicParseTableProvider(Descriptor descriptor, String providerFunction) {
		super(descriptor);
		this.providerFunction = providerFunction;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public void setController(SGLRParseController controller) {
		this.controller = controller;
	}
	
	@Override
	public void initialize(File input) {
		try {
			assert controller != null;
			if (runtime == null)
				runtime = getDescriptor().createService(StrategoObserver.class, controller);
			runtime.getLock().lock();
			try {
				IResource resource = EditorIOAgent.getResource(input);
				IStrategoTerm pathTerm = Environment.getTermFactory().makeString(input.getAbsolutePath());
				IStrategoTerm table = runtime.invokeSilent(providerFunction, pathTerm, resource);
				if (isTermString(table)) {
					setTable(Environment.loadParseTable(asJavaString(table)));
				} else {
					Environment.logException("Path to existing parse table expected from table provider: " + table);
				}
			} finally {
				runtime.getLock().unlock();
			}
		} catch (BadDescriptorException e) {
			Environment.logException("Unable to load parse table provider", e);
		} catch (InvalidParseTableException e) {
			Environment.logException("Unable to load table provided by parse provider", e);
		} catch (IOException e) {
			Environment.logException("Unable to load table provided by parse provider", e);
		}
	}
}
