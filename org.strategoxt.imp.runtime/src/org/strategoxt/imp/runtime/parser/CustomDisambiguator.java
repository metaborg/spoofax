package org.strategoxt.imp.runtime.parser;

import static org.spoofax.interpreter.core.Tools.isTermTuple;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.putImploderAttachment;
import static org.spoofax.terms.TermVisitor.tryGetListIterator;
import static org.spoofax.terms.attachments.OriginAttachment.getOrigin;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getParseController;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.Tokenizer;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * A class that uses the language runtime to disambiguate an AST.
 *
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class CustomDisambiguator {

	private final SGLRParseController controller;

	private final String[] functions;

	private WeakReference<StrategoObserver> runtime;

	public CustomDisambiguator(SGLRParseController controller, String[] functions) {
		this.controller = controller;
		this.functions = functions;
	}

	public IStrategoTerm disambiguate(IStrategoTerm ast) {
		if (functions.length == 0)
			return ast;

		if (Environment.isMainThread()) {
			// Shouldn't acquire environment lock from main thread
			controller.scheduleParserUpdate(SGLRParseController.REPARSE_DELAY, false);
			throw new OperationCanceledException("Cannot parse and disambiguate from main thread");
		}

		StrategoObserver myRuntime = getRuntime();
		if (myRuntime == null)
			return ast;

		myRuntime.getLock().lock();
		try {
			IResource resource = getResource(ast);
			for (String f : functions) {
				IStrategoTerm input = myRuntime.getInputBuilder().makeInputTerm(ast, false);
				IStrategoTerm result = myRuntime.invokeSilent(f, input, resource);
				if (result == null) {
					myRuntime.reportRewritingFailed();
					Environment.logException("Disambiguation failed (see error log)");
				} else {
					ast = transferAttachments(ast, result);
				}
			}
		} finally {
			myRuntime.getLock().unlock();
		}

		return ast;
	}

	private static IStrategoTerm transferAttachments(final IStrategoTerm oldTerm, IStrategoTerm newTerm) {
		reinitTokens(newTerm, getLeftToken(oldTerm), getRightToken(oldTerm));
		getTokenizer(oldTerm).setAst(newTerm);
		getTokenizer(oldTerm).initAstNodeBinding();
		ParentAttachment.putParent(newTerm, null, null);
		reinitParents(newTerm);
		SourceAttachment.putSource(newTerm, getResource(oldTerm), getParseController(oldTerm));
		return newTerm;
	}

	private static void reinitParents(IStrategoTerm parent) {
		Iterator<IStrategoTerm> iterator = tryGetListIterator(parent);
		for (int i = 0, max = parent.getSubtermCount(); i < max; i++) {
			IStrategoTerm child = iterator == null ? parent.getSubterm(i) : iterator.next();
			ParentAttachment.putParent(child, parent, null);
			reinitParents(child);
		}
	}

	/**
	 * Assign tokens to this term and all subterms, using the origin tokens or the given tokens.
	 *
	 * @param left  The left token to use if no token can be identified for this term.
	 * @param right The right token to use if no token can be identified for this term.
	 */
	private static void reinitTokens(IStrategoTerm term, IToken left, IToken right) {
		// Init tokens
		if (ImploderAttachment.get(term) == null) {
			IStrategoTerm origin = getOrigin(term);
			if (origin != null) {
				ImploderAttachment old = ImploderAttachment.get(origin);
				String sort = isListOrTuple(origin) ? old.getElementSort() : old.getSort();
				left = old.getLeftToken();
				right = old.getRightToken();
				putImploderAttachment(term, isListOrTuple(term), sort, left, right);
			} else {
				putImploderAttachment(term, isListOrTuple(term), null, left, right);
			}
		}
		term.removeAttachment(OriginAttachment.TYPE);

		// Recurse
		Iterator<IStrategoTerm> iterator = tryGetListIterator(term);
		for (int i = 0, max = term.getSubtermCount(); i < max; i++) {
			IStrategoTerm child = iterator == null ? term.getSubterm(i) : iterator.next();
			reinitTokens(child, left, right);
			left = Tokenizer.getTokenAfter(getRightToken(child));
		}
	}

	private static boolean isListOrTuple(IStrategoTerm term) {
		return term.isList() || isTermTuple(term);
	}

	private StrategoObserver getRuntime() {
		StrategoObserver result = runtime == null ? null : runtime.get();
		try {
			if (result == null) {
				Descriptor descriptor = Environment.getDescriptor(controller.getLanguage());
				result = descriptor.createService(StrategoObserver.class, controller);
				runtime = new WeakReference<StrategoObserver>(result);
			}
			return result;
		} catch (BadDescriptorException e) {
			Environment.logException("Could not load observer", e);
			return null;
		}
	}
}
