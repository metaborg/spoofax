package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.ContentProposalTemplate;
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
		ContentProposalTemplate[] templates = readCompletionTemplates(descriptor);
		for (ContentProposalTemplate template : templates) {
			completionKeywords.remove(template.getPrefix());
		}
		String[] keywords = completionKeywords.toArray(new String[0]);

		registerListener(descriptor, controller);
		
		return new ContentProposer(feedback, completionFunction, identifierLexical, keywords, templates);
	}

	private static void registerListener(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		ISourceViewer viewer = controller.getEditor().getEditor().getServiceControllerManager().getSourceViewer();
		Set<Pattern> triggers = readTriggers(descriptor);
		ContentProposerListener.register(triggers, viewer);
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

	private static ContentProposalTemplate[] readCompletionTemplates(Descriptor descriptor) {
		Set<ContentProposalTemplate> results = new HashSet<ContentProposalTemplate>();

		for (IStrategoAppl template : collectTerms(descriptor.getDocument(), "CompletionTemplate")) {
			IStrategoTerm prefixTerm = termAt(template, 0);
			String prefix = termContents(prefixTerm);
			IStrategoList completionParts = termAt(template, 1);
			completionParts = Environment.getTermFactory().makeListCons(prefixTerm, completionParts);
			results.add(new ContentProposalTemplate(prefix, completionParts));
		}
		
		return results.toArray(new ContentProposalTemplate[0]);
	}

	private static Set<Pattern> readTriggers(Descriptor descriptor) throws BadDescriptorException {
		Set<Pattern> results = new HashSet<Pattern>();
		
		for (IStrategoAppl trigger : collectTerms(descriptor.getDocument(), "CompletionTrigger")) {
			try {
				String pattern = termContents(termAt(trigger, 0));
				Pattern compiledPattern = Pattern.compile(pattern);
				results.add(compiledPattern);
			} catch (PatternSyntaxException e) {
				throw new BadDescriptorException("Illegal trigger pattern in editor descriptor");
			}
		}
		return results;
	}
}
