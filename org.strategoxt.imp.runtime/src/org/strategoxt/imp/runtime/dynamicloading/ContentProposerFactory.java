package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.imp.runtime.services.ContentProposerListener;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerFactory extends AbstractServiceFactory<IContentProposer> {

	private static final String DEFAULT_LEXICAL = "[A-Za-z_0-9]+";
	
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

		Pattern completionPattern = readCompletionPattern(descriptor);		
		Set<String> completionKeywords = readCompletionKeywords(descriptor);
		String[] keywords = completionKeywords.toArray(new String[0]);

		registerListener(descriptor, controller);
		
		return new ContentProposer(feedback, completionFunction, completionPattern, keywords);
	}

	private static void registerListener(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		ISourceViewer viewer = controller.getEditor().getEditor().getServiceControllerManager().getSourceViewer();
		Set<Pattern> triggers = readTriggers(descriptor);
		ContentProposerListener.register(triggers, viewer);
	}

	private static Pattern readCompletionPattern(Descriptor descriptor) throws BadDescriptorException {
		try {
			String completionLexical = descriptor.getProperty("CompletionLexical", DEFAULT_LEXICAL);
			Pattern result = Pattern.compile(completionLexical);
			if (result.matcher("").matches())
				throw new PatternSyntaxException("Completion lexical matches the empty string", completionLexical, 0);
			if (!result.matcher(ContentProposer.COMPLETION_TOKEN).matches())
				throw new PatternSyntaxException("Completion lexical must allow letters and numbers (e.g., "
						+ ContentProposer.COMPLETION_TOKEN + ")", completionLexical, 0);
			return result;
		} catch (PatternSyntaxException e) {
			throw new BadDescriptorException("Illegal completion lexical in editor descriptor", e);
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
