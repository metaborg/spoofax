package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.imp.services.IContentProposer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerFactory extends AbstractServiceFactory<IContentProposer> {

	private static final String DEFAULT_LEXICAL = "[A-Za-z_0-9]+";
	
	public ContentProposerFactory() {
		super(IContentProposer.class);
	}
	
	@Override
	public IContentProposer create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		String completionFunction = descriptor.getProperty("CompletionProposer", null);
		String completionLexical = descriptor.getProperty("CompletionLexical", DEFAULT_LEXICAL);
		StrategoObserver feedback = descriptor.createService(StrategoObserver.class, controller);

		Pattern completionPattern;
		try {
			completionPattern = Pattern.compile(completionLexical);
			if (completionPattern.matcher("").matches())
				throw new PatternSyntaxException("Completion lexical matches the empty string", completionLexical, 0);
			if (!completionPattern.matcher(ContentProposer.createCompletionToken()).matches())
				throw new PatternSyntaxException("Completion lexical allow letters and numbers", completionLexical, 0);
		} catch (PatternSyntaxException e) {
			throw new BadDescriptorException("Illegal completion lexical in editor descriptor", e);
		}
		Set<String> completionKeywords = new HashSet<String>();
		
		for (IStrategoAppl keyword : collectTerms(descriptor.getDocument(), "CompletionKeyword")) {
			String literal = termContents(termAt(keyword, 0));
			IStrategoAppl type = termAt(keyword, 1);
			if (cons(type).equals("Disable"))
				completionKeywords.remove(literal);
			else
				completionKeywords.add(literal);
		}
		
		String[] keywords = completionKeywords.toArray(new String[0]); 
		
		return new ContentProposer(feedback, completionFunction, completionPattern, keywords);
	}

}
