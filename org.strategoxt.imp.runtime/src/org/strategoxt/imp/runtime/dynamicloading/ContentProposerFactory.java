package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.Completion;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.imp.runtime.services.ContentProposerListener;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerFactory extends AbstractServiceFactory<IContentProposer> {

	public ContentProposerFactory() {
		super(IContentProposer.class, true);
	}

	/**
	 * Eagerly initializes the content proposer service listener.
	 * (Normally, it is only initialized after the user hits control-space,
	 *  but we want it to be triggered by other events as well.)
	 */
	public static void eagerInit(Descriptor descriptor, IParseController controller, EditorState editor) {
		try {
			if (editor != null && controller instanceof SGLRParseController) {
				((SGLRParseController) controller).setEditor(editor);
				registerListener(descriptor, (SGLRParseController) controller);
			}
		} catch (BadDescriptorException e) {
			Environment.logException("Could not eagerly initialize the content proposal service", e);
		} catch (RuntimeException e) {
			Environment.logException("Could not eagerly initialize the content proposal service", e);
		}
	}

	@Override
	public IContentProposer create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		String completionFunction = descriptor.getProperty("CompletionProposer", null);
		StrategoObserver feedback = descriptor.createService(StrategoObserver.class, controller);

		Pattern identifierLexical = SyntaxPropertiesFactory.readIdentifierLexical(descriptor, true);

		Set<String> completionKeywords = readCompletionKeywords(descriptor);
		Set<Completion> templates = readCompletionTemplates(descriptor);

		for (Completion template : templates) {
			completionKeywords.remove(template.getPrefix());
		}
		for (String keyword : completionKeywords) {
			templates.add(Completion.makeKeyword(keyword));
		}

		registerListener(descriptor, controller);

		return new ContentProposer(feedback, completionFunction, identifierLexical, templates);
	}

	private static void registerListener(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {

		try {
			UniversalEditor editor = controller.getEditor().getEditor();
			ISourceViewer viewer = editor.getServiceControllerManager().getSourceViewer();
			Set<Pattern> triggers = readTriggers(descriptor);
			ContentProposerListener.register(triggers, viewer);
		} catch (RuntimeException e) {
			Environment.logWarning("Exception while trying to register content proposer listener", e);
		}
	}

	private static Set<String> readCompletionKeywords(Descriptor descriptor) {
		Set<String> results = new HashSet<String>();

		for (IStrategoAppl keyword : collectTerms(descriptor.getDocument(), "CompletionKeyword")) {
			String literal = termContents(termAt(keyword, 0));
			IStrategoAppl type = termAt(keyword, 1);
			if (cons(type).equals("Disable"))
				results.remove(literal);
			else
				results.add(literal);
		}
		return results;
	}

	private static Set<Completion> readCompletionTemplates(Descriptor descriptor) {
		Set<Completion> results = new HashSet<Completion>();

		for (IStrategoAppl template : collectTerms(descriptor.getDocument(), "CompletionTemplate")) {
			results.add(parseContentProposalTemplate(template, 0, null));
		}

		for (IStrategoAppl template : collectTerms(descriptor.getDocument(), "CompletionTemplateWithSort")) {
			String sort = termContents(termAt(template, 0));
			results.add(parseContentProposalTemplate(template, 1, sort));
		}

		for (IStrategoAppl template : collectTerms(descriptor.getDocument(), "CompletionTemplateEx")) {
			parseContentProposalTemplateEx(template, results);
		}

		return results;
	}

	private static Completion parseContentProposalTemplate(IStrategoAppl template, int index, String sort) {
		ITermFactory factory = Environment.getTermFactory();
		IStrategoTerm prefixTerm = termAt(template, index + 0);
		IStrategoList completionParts = termAt(template, index + 1);
		IStrategoTerm anno = termAt(template, index + 2);
		completionParts = factory.makeListCons(prefixTerm, completionParts);
		return Completion.makeTemplate(null, sort, completionParts, "Blank".equals(cons(anno)), false);
	}

	private static void parseContentProposalTemplateEx(IStrategoAppl template, Set<Completion> results) {
		IStrategoList parts = termAt(template, 2);
		String prefix = termContents(termAt(template, 1));
		IStrategoList annos = termAt(template, 3);
		boolean blank = isConsInList(annos, "Blank");
		boolean linked = isConsInList(annos, "Linked");

		IStrategoList sorts = termAt(template, 0);
		if (sorts.isEmpty()) {
			results.add(Completion.makeTemplate(prefix, null, parts, blank, linked));
		}
		else {
			for (; !sorts.isEmpty(); sorts = sorts.tail()) {
				String sort = termContents(sorts.head());
				results.add(Completion.makeTemplate(prefix, sort, parts, blank, linked));
			}
		}
	}

	private static boolean isConsInList(IStrategoList list, String cons) {
		for (; !list.isEmpty(); list = list.tail()) {
			IStrategoTerm term = list.head();
			if (cons.equals(cons(term))) return true;
		}
		return false;
	}

	private static Set<Pattern> readTriggers(Descriptor descriptor) throws BadDescriptorException {
		Set<Pattern> results = new HashSet<Pattern>();

		for (IStrategoAppl trigger : collectTerms(descriptor.getDocument(), "CompletionTrigger")) {
			try {
				String pattern = termContents(termAt(trigger, 0));
				if (".".equals(pattern))
					pattern = "\\."; // common mistake
				Pattern compiledPattern = Pattern.compile(pattern);
				results.add(compiledPattern);
			} catch (PatternSyntaxException e) {
				throw new BadDescriptorException("Illegal trigger pattern in editor descriptor");
			}
		}
		return results;
	}
}
