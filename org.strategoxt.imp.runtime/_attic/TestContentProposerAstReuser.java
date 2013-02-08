package org.strategoxt.imp.runtime.services;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.junit.Before;
import org.junit.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

public class TestContentProposerAstReuser {

	private static final Pattern identifierLexical = Pattern.compile("[A-Za-z0-9]+");

	private final IStrategoTerm term = TermFactory.EMPTY_LIST;

	private ContentProposerAstReuser cpar;

	@Before
	public void setUp() {
		cpar = new ContentProposerAstReuser(identifierLexical);
		assertNull(cpar.tryReusePreviousAst(new Position(3), "hel"));
		cpar.storeAstForReuse(term, term, "hel");
	}

	@Test
	public void completionNodeAndPrefix() {
		assertNull(cpar.getCompletionNode());
		assertNull(cpar.getCompletionPrefix());
	}

	@Test
	public void reuseWithoutMove() {
		assertSame(term, cpar.tryReusePreviousAst(new Position(3), "hel"));
		assertSame(term, cpar.getCompletionNode());
		assertEquals("hel", cpar.getCompletionPrefix());
	}

	@Test
	public void reuseWithMoveForward() {
		assertSame(term, cpar.tryReusePreviousAst(new Position(4), "hell"));
	}

	@Test
	public void reuseWithMoveBackward() {
		assertSame(term, cpar.tryReusePreviousAst(new Position(2), "he"));
	}

	@Test
	public void reuseWithMoveForwardMuch() {
		assertNull(cpar.tryReusePreviousAst(new Position(5), "hello"));
	}

	@Test
	public void reuseWithMoveBackwardMuch() {
		assertNull(cpar.tryReusePreviousAst(new Position(1), "h"));
	}

	@Test
	public void reuseWithChangedText() {
		assertNull(cpar.tryReusePreviousAst(new Position(3), "foo"));
	}
}
