package org.strategoxt.imp.runtime.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.spoofax.jsglr.client.Frame;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.ArrayDeque;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Calculates syntactic content completion suggestions.
 *
 * @author Maartje de Jonge
 */
public class ContentProposerSyntactic {

	private static final String ARTIFICIAL_SORT_STRING_END = "|";

	private static final String ARTIFICIAL_SORT_STRING_START = "|#";

	private static final String WHITESPACE_SEPARATION = "     ";

	private static final int RIGHT_CONTEXT_SIZE = 80;

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private final Set<Completion> templates; //available templates
	
	
	private Set<Completion> acceptedProposals; //results (positive)

	private Set<Completion> rejectedProposals; //results (negative)

	private String errorMessage; //error results
	
	
	private ParseConfigReuser sglrReuser; //for reuse purpose
	
	private String documentPrefix; //for reuse purpose

	private String documentSuffix; //for reuse purpose

	private final Map<String, Boolean> testedSorts; //for reuse purpose
	
	public ContentProposerSyntactic(Set<Completion> templates, ParseConfigReuser sglrReuser){
		this.templates = removeArtificialSortTemplates(templates);
		this.errorMessage = null;
		this.acceptedProposals = new java.util.HashSet<Completion>();
		this.rejectedProposals = new java.util.HashSet<Completion>();
		this.sglrReuser = sglrReuser;
		this.testedSorts = new HashMap<String, Boolean>();
	}
	
	private Set<Completion> removeArtificialSortTemplates(Set<Completion> templates) {
		Set<Completion> results = new java.util.HashSet<Completion>();
		for (Completion template : templates) {
			if(!isArtificialTemplate(template)){
				results.add(template);
			}
		}
		return results;
	}
	
	private boolean isArtificialTemplate(Completion template){
		return template.getPrefix().startsWith(ARTIFICIAL_SORT_STRING_START) && template.getPrefix().endsWith(ARTIFICIAL_SORT_STRING_END);
	}

	public Set<Completion> getSyntacticCompletions(final SGLRParseController parseController,
			String documentPrefix, String completionPrefix, String documentSuffix) {
		this.acceptedProposals = null;
		this.rejectedProposals.clear();
		Set<Completion> acceptedProposalResults = new HashSet<Completion>();
		for (Completion proposal : templates) {
			if (proposal.extendsPrefix(completionPrefix)) {
				if (!proposal.isBlankLineRequired() || isBlankBeforeOffset(documentPrefix, documentPrefix.length())){
					resetAcceptedSort(documentPrefix, documentSuffix);
					if(checkSyntacticContext(parseController, documentPrefix, proposal, documentSuffix)){ 
						acceptedProposalResults.add(proposal); 
						//TODO: filter on indent?? e.g. only allow list sorts at same indent
						//idea: isListSort? traverse parent chain and inspect indentation and list sort
					}
					else{
						rejectedProposals.add(proposal);
					}
				}
			}
		}
		this.acceptedProposals = acceptedProposalResults;
		return acceptedProposalResults;
	}

	private void resetAcceptedSort(String documentPrefix, String documentSuffix) {
		if(!documentPrefix.equals(this.documentPrefix) || !documentSuffix.equals(this.documentSuffix)){
			this.testedSorts.clear();
			this.documentPrefix = documentPrefix;
			this.documentSuffix = documentSuffix;
		}
	}

	private static boolean isBlankBeforeOffset(String document, int offset) {
		for (int i = offset - 1; i > 0; i--) {
			switch (document.charAt(i)) {
				case ' ': case '\t': continue;
				case '\n': return true;
				default: return false;
			}
		}
		return true;
	}


	private boolean checkSyntacticContext(SGLRParseController parseController, String documentPrefix, Completion proposal, String documentSuffix) {						
		SGLR parser = new SGLR(parseController.getParser().getParser().getTreeBuilder(), parseController.getParser().getParser().getParseTable());
		parseController.scheduleParserUpdate(REINIT_PARSE_DELAY, true); // cancel current parse
		parseController.getParseLock().lock();
		try {
			//artificial sort string, e.g. |#Statement|, or concatenated text parts
			String proposalString = createProposalParseString(proposal);
			
			if(this.testedSorts.get(proposalString) != null){
				return this.testedSorts.get(proposalString).booleanValue();
			}
			
			boolean isAccept = 
				//Syntactic context can not be checked due to (unrecovered) syntax errors in left context.
				//default: only show suggestions that are syntactically checked.
				parseLeftContext(parser, documentPrefix) &&

				// LEFT CONTEXT CRITERION: 
				// Inserting the proposal string must result in a valid program prefix
				// (The program prefix before the proposal may be parsed using error recovery).				
				leftContextAcceptsProposal(parser, documentPrefix, proposalString, documentSuffix) &&

				//RIGHT-CONTEXT CRITERION: 
				//For list elements (or constructs/keywords that start the list construct and have the list Sort attached): 
				//inserting the proposal string must not invalidate the right context.
				//I.e. if the right context parses without the proposal text, then it should also parse with the proposal text inserted.
				//REMARK: The criterion does not apply to keywords and constructs that do not have an attached list sort, 
				//e.g. inserting the "else" keyword typically does break the right context even though it is expected there,
				//or: inserting a construct at the start of a list element that has its own sort attached typically does break the right context.				
				((!proposal.isListSort()) || rightContextAcceptsProposal(parser, documentPrefix, proposalString, documentSuffix));
			
			this.testedSorts.put(proposalString, isAccept);
			return isAccept;
		} catch (Exception e) {
			Environment.logException("Something went wrong during reparsing for syntactic content completion", e);
			if (errorMessage == null || errorMessage.equals(""))
				errorMessage = "No syntactic proposals available - parse errors";
		} finally {
			parser.setCompletionParse(false, -1);
			parseController.getParseLock().unlock();
		}
		return false;
	}

	private String createProposalParseString(Completion proposal) {
		String sort = proposal.getSort();
		String proposalString; 
		if(sort != null){
			proposalString = createArtificialParseStringForSort(sort);
		}
		else{
			proposalString = proposal.getNewText();
		}
		assert proposalString != null;
		return proposalString;
	}

	public static String createArtificialParseStringForSort(String sort) {
		if(sort == null || sort.equals(""))
			return null;
		String proposalString = ARTIFICIAL_SORT_STRING_START + sort + ARTIFICIAL_SORT_STRING_END;
		return proposalString;
	}

	private boolean leftContextAcceptsProposal(SGLR parser, String documentPrefix, String proposalString, String documentSuffix) {
		parser.setCompletionParse(true, documentPrefix.length() + 3);
		String proposedDocument = documentPrefix + proposalString + WHITESPACE_SEPARATION + documentSuffix;
		int endOffsetProposal = (documentPrefix + proposalString).length();
		boolean validLeftContextProposal = checkSyntax(parser, endOffsetProposal + 1, proposedDocument);
		return validLeftContextProposal;
	}

	private boolean rightContextAcceptsProposal(SGLR parser, String documentPrefix, String proposalString, String documentSuffix) {
		parser.setCompletionParse(true, documentPrefix.length() + 3);
		String currentDocument = documentPrefix + WHITESPACE_SEPARATION + documentSuffix;
		String proposedDocument = documentPrefix + proposalString + WHITESPACE_SEPARATION + documentSuffix;
		int endOffsetProposal = (documentPrefix + proposalString).length();	
		
		//TODO: proper implementation of "failureOffset proposedDocument < failureOffset Document"
		for (int i = 1; i <= 4; i++) {
			int endOffsetProposalRightContext = Math.min(endOffsetProposal + RIGHT_CONTEXT_SIZE/i, proposedDocument.length() + 1);
			int endOffsetCurrentRightContext  = Math.min(documentPrefix.length() + RIGHT_CONTEXT_SIZE/i, currentDocument.length()); //Remark: RC-Current correct without EOF, RC-Proposal correct with EOF
			boolean checkRightContextCurrent  = checkSyntax(parser, endOffsetCurrentRightContext, currentDocument);
			boolean checkRightContextProposed = checkSyntax(parser, endOffsetProposalRightContext, proposedDocument);
			if(checkRightContextCurrent && !checkRightContextProposed) {
				return false; 
			}
		}		
		return true;
	}

	private boolean checkSyntax(SGLR parser, int endOffset, String document) {
		ArrayDeque<Frame> parserConfig = 
		  sglrReuser.parsePrefix(parser, false, false, document, endOffset);
		return !parserConfig.isEmpty();
	}

	private boolean parseLeftContext(SGLR parser, String documentPrefix) {
		int endOffsetLeftContext = Math.max(documentPrefix.length() - 3, 0); //no complications with look ahead
		if (endOffsetLeftContext <= 0){
			return true;
		}
		ArrayDeque<Frame> leftPrefixStacks = sglrReuser.parsePrefix(parser, true, true, documentPrefix, endOffsetLeftContext);
		if(leftPrefixStacks.isEmpty()){
			errorMessage = "No syntactic proposals available - syntax errors";
			return false;
		}
		return true;
	}
	
	public boolean hasErrors(){
		return this.errorMessage != null && (this.acceptedProposals == null || this.acceptedProposals.isEmpty());
	}
	
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	public SortedSet<String> getAcceptedSorts(){
		SortedSet<String> sorts = new java.util.TreeSet<String>();
		for (Completion completion : this.acceptedProposals) {
			if(completion.getSort()!= null){
				String sortString = completion.getSort();
				if(completion.isListSort())
					sortString += "*";
				sorts.add(sortString);
			}
			else
				sorts.add(completion.getNewText().trim());				
		}
		return sorts;
	}

	public SortedSet<String> getRejectedSorts(){
		SortedSet<String> sorts = new java.util.TreeSet<String>();
		for (Completion completion : this.rejectedProposals) {
			if(completion.getSort()!= null)
				sorts.add(completion.getSort());
			else
				sorts.add(completion.getNewText().trim());				
		}
		return sorts;
	}
	
	public Set<Completion> getResults() {
		return acceptedProposals;
	}

	public SortedSet<String> missingSortTemplates(){
		SortedSet<String> templatesWithoutSort = new java.util.TreeSet<String>();
		for (Completion template : templates) {
			if(template.getSort() == null){
				templatesWithoutSort.add(template.getNewText().trim());
			}
		}
		return templatesWithoutSort;
	}
	
	public Set<Completion> getTemplateProposalsForSort(SGLRParseController parseController, String wantedSort) {
		// Add templates for sorts injected into wantedSort.
		//  `sort -> wantedSort' => add templates for sort
		//  `sub -> sort' => add templates for sub too

		final Set<Completion> results = new HashSet<Completion>();
		Set<String> wantedSorts = sglrReuser.getInjectionsFor(wantedSort); //injections.get(wantedSort);

		for (Completion proposal : templates) {
			if (wantedSorts.contains(proposal.getSort())) {
				results.add(proposal);
			}
		}
		return results;
	}

}
