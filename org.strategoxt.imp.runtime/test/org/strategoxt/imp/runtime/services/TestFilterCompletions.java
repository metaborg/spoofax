package org.strategoxt.imp.runtime.services;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the ContentProposer.filterCompletions method.
 *
 * @author Tobi Vollebregt
 */
public class TestFilterCompletions {

	static final Pattern identifierLexical = Pattern.compile("[A-Za-z][A-Za-z0-9]*"); 

	static final Set<String> noSorts = Collections.emptySet();

	ContentProposer cp;
	Set<Completion> completions;
	ICompletionProposal[] results;

	@Before
	public void setUp() throws Exception {
		cp = new ContentProposer(null, null, identifierLexical, null);
		completions = new HashSet<Completion>();
		results = null;
	}

	/**
	 * @param document The document as it is when completion is triggered
	 * @param prefix   This should be the identifier up to offset in document 
	 * @param offset   Offset in document at which completion is triggered
	 * @param length   Length of the selection
	 * @param sorts    Sorts allowed at this position 
	 */
	void filterCompletions(String document, String prefix, int offset, int length, Set<String> sorts) {
		results = cp.filterCompletions(completions, document, prefix, new Position(offset, length), sorts, null);
	}

	@Test
	public void testNoCompletionsNoPrefix() {
		filterCompletions("", "", 0, 0, noSorts);
		assertEquals(0, results.length);
	}

	@Test
	public void testAllCompletionsNoPrefix() {
		completions.add(Completion.makeTemplate("foo", null));
		completions.add(Completion.makeTemplate("bar", null));
		filterCompletions("", "", 0, 0, noSorts);
		assertEquals(2, results.length);
	}
	
	@Test
	public void testPrefix() {
		completions.add(Completion.makeTemplate("foo", null));
		completions.add(Completion.makeTemplate("oof", null));
		filterCompletions("fo", "fo", 2, 0, noSorts);
		assertEquals(1, results.length);
		assertEquals("foo", results[0].getDisplayString());
	}

	@Test
	public void testSelectedPrefix() {
		completions.add(Completion.makeTemplate("foo", null));
		completions.add(Completion.makeTemplate("oof", null));
		filterCompletions("fo", "fo", 0, 2, noSorts);
		assertEquals(1, results.length);
		assertEquals("foo", results[0].getDisplayString());	
	}
	
	@Test
	public void testNonIdentifier() {
		completions.add(Completion.makeTemplate(", avoid", null));
		filterCompletions("", "", 0, 0, noSorts);
		assertEquals(1, results.length);
	}
	
	@Test
	public void testNonIdentifierPrefix() {
		completions.add(Completion.makeTemplate(", avoid", null));
		filterCompletions(", ", ", ", 1, 0, noSorts);
		assertEquals(1, results.length);
	}

	@Test
	public void testNonIdentifierLongPrefix() {
		completions.add(Completion.makeTemplate("<?= foo", null));
		filterCompletions("<?=", "", 2, 0, noSorts);
		assertEquals(1, results.length);
	}

	@Test
	public void testNonIdentifierSemanticProposal() {
		completions.add(Completion.makeSemantic("\"aaa\"", null));
		filterCompletions("", "", 0, 0, noSorts);
		assertEquals(1, results.length);
	}
	
	@Test
	public void testNonIdentifierSemanticProposalPrefix() {
		completions.add(Completion.makeSemantic("\"aaa\"", null));
		filterCompletions("\"a", "\"a", 0, 0, noSorts);
		assertEquals(1, results.length);
	}
	
	@Test
	public void testFilterSorts() {
		completions.add(Completion.makeTemplate("foo", "Foo"));
		filterCompletions("", "", 0, 0, noSorts);
		assertEquals(0, results.length);

		Set<String> sorts = new HashSet<String>();
		sorts.add("Foo");
		filterCompletions("", "", 0, 0, sorts);
		assertEquals(1, results.length);
	}
}
