package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.MUTABLE;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.interpreter.terms.IStrategoTerm.TUPLE;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;
import static org.strategoxt.imp.runtime.Environment.getTermFactory;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Frame;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.Tokenizer;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.ArrayDeque;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermTransformer;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.attachments.ParentTermFactory;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.TermReader;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * Calculates semantic content completion suggestions.
 *
 * @author Maartje de Jonge
 */
public class ContentProposerSemantic {

	public static final IStrategoConstructor COMPLETION_CONSTRUCTOR =
			getTermFactory().makeConstructor("COMPLETION", 1);

	public static final IStrategoConstructor COMPLETION_UNKNOWN =
			getTermFactory().makeConstructor("NOCONTEXT", 1);

	public static final String COMPLETION_TOKEN = "completion123";

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private static final String WHITESPACE_SEPARATION = "     ";
	
	private static final int RIGHT_CONTEXT_SIZE = 40;

	private final StrategoObserver observer;

	private final String completionFunction; //stratego rule that implements completion transformation

	//TODO: let user specify what constructs are preferred as context, 
	// e.g. Assign(y,Var(x)) may be more interesting than Var(x)
	// because the type info of y can be taken into account
	private final IStrategoTerm[] semanticNodes; //determine the syntactic context that is returned, e.g. the first term of the lhs of the rule

	
	private ParseConfigReuser sglrReuser; //for reuse purpose
	
	private String documentPrefix; //for reuse purpose

	private String documentSuffix; //for reuse purpose

	private Set<Completion> proposals; //for reuse purpose, proposals for the empty string prefix

	
	private Set<Completion> results; //positive results, proposals filtered for completion prefix

	
	private String errorMessage; //error results

	
	private IStrategoTerm currentAST; //intermediate results
	
	private int startOffsetCompletionToken; //intermediate results

	private Set<IStrategoTerm> completionContexts; //intermediate results


	public ContentProposerSemantic(StrategoObserver observer, String completionFunction, IStrategoTerm[] semanticNodes, ParseConfigReuser sglrReuser){
		this.completionFunction = completionFunction;
		this.observer = observer;
		this.semanticNodes = extendWithInjections(semanticNodes);
		this.documentPrefix = "";
		this.documentSuffix = "";
		this.proposals = new java.util.HashSet<Completion>();
		this.errorMessage = null;
		this.results = new java.util.HashSet<Completion>();
		this.completionContexts = new java.util.HashSet<IStrategoTerm>();
		this.sglrReuser = sglrReuser;
		
		this.currentAST = null;
		this.startOffsetCompletionToken = -1;
	}

	public Set<Completion> getSemanticCompletions(IParseController controller, String documentPrefix, String completionPrefix, String documentSuffix) { 
		this.results = null;
		if(!this.documentPrefix.equals(documentPrefix) || !this.documentSuffix.equals(documentSuffix)){ 
			//clear results
			this.completionContexts.clear();
			this.proposals.clear();
			errorMessage = null;
			
			//calculate 'empty string' proposals
			Set<Completion> proposals = calculateProposals(controller, documentPrefix, completionPrefix, documentSuffix, this.semanticNodes, false);

			//store results for reuse
			this.proposals = proposals;
			this.documentPrefix = documentPrefix;
			this.documentSuffix = documentSuffix;
		}	
		this.results = filterProposals(completionPrefix);
		return this.results;
	}


	private Set<Completion> calculateProposals(IParseController controller, String documentPrefix,
			String completionPrefix, String documentSuffix, IStrategoTerm[] semanticNodes, boolean isRequiredMatch) {
		Set<IStrategoTerm> inputTerms = constructCompletionInputTerms(ContentProposer.getParser(controller), 
				documentPrefix, completionPrefix, documentSuffix, semanticNodes, isRequiredMatch);
		Set<IStrategoTerm> proposalLists = constructOutputTerms(controller, inputTerms);						
		Set<Completion> proposals = constructProposals(proposalLists);

		//store results for print tip
		setCompletionContexts(inputTerms);
		return proposals;
	}
		
	private Set<IStrategoTerm> constructCompletionInputTerms(SGLRParseController parseController, 
			String documentPrefix, String completionPrefix, String documentSuffix, 
			IStrategoTerm[] semanticNodes, boolean isRequiredMatch) {	

		this.startOffsetCompletionToken = documentPrefix.length(); 

		//constructs (partial) trees around the completion offset, the trees represent possible syntactic contexts.
		//syntactic contexts are focused on the direct context (taking into account semantic nodes),
		//so that they can be used as input to the editor-complete rule.
		//Also constructs and stores the AST of the document with the completion token filled in.
		Set<IStrategoTerm> completionContexts = constructCompletionContexts(parseController, documentPrefix, completionPrefix, documentSuffix); 
		
		ArrayList<NodeMapping<String>> mappings = InputTermBuilder.createNodeMappings(semanticNodes);
		completionContexts = focusCompletionContexts(completionContexts, mappings, isRequiredMatch); 
		
		//System.out.println(completionContexts);

		//Build input tuples for editor-complete rule, e.g. (analyzed-context, position, analyzed-ast, path, project-path)
		//Completion nodes are constructed with the empty string (instead of completionPrefix) to enable reuse of results.
		//Completion nodes are injected in the original tree, nearby the cursor offset.
		Set<IStrategoTerm> basicInputTerms = constructInputTerms(completionContexts); //example: (Var("completion123"{[NS, "completion123", scope]}"), ...)
		Set<IStrategoTerm> inputTerms = putCompletionNodes(basicInputTerms, ""); //example: Var(COMPLETION(""){[NS, "completion123", scope]})
		
		return inputTerms;
	}

	private Set<IStrategoTerm> constructCompletionContexts(SGLRParseController parseController, 
			String documentPrefix, String completionPrefix, String documentSuffix) {
		Set<IStrategoTerm> completionContexts = new java.util.HashSet<IStrategoTerm>();
		parseController.scheduleParserUpdate(REINIT_PARSE_DELAY, true); // cancel current parse
		parseController.getParseLock().lock();
		try {
			//sets AST for document (possible a slightly inferior AST with the empty string parsed instead of the completion identifier) 
			currentAST = parseController.getCurrentAst(); 
			if(this.currentAST == null)
				return new java.util.HashSet<IStrategoTerm>();

			//constructed partial trees around cursor location. 
			//Only take into account the left context, since the right context can not be trusted
			//SGLR sglr = parseController.getParser().getParser();
			String documentFromPrefix = documentPrefix + COMPLETION_TOKEN + WHITESPACE_SEPARATION;
			completionContexts = parseCompletionContext(parseController, documentFromPrefix, documentFromPrefix.length() - 2);			
			String fullDocument = documentPrefix + COMPLETION_TOKEN + documentSuffix;
			int endOffset = Math.min(fullDocument.length() - 2, (documentPrefix + COMPLETION_TOKEN).length() + RIGHT_CONTEXT_SIZE);
			Set<IStrategoTerm> moreCompletionContexts = parseCompletionContext(parseController, fullDocument, endOffset);
			completionContexts.addAll(moreCompletionContexts);
		} catch (Exception e) {
			this.errorMessage = "No semantic proposals available - syntax errors";
			Environment.logException(errorMessage, e);
		} finally {
			parseController.getParseLock().unlock();
		}
		return completionContexts;
	}

	private Set<IStrategoTerm> parseCompletionContext(SGLRParseController parseController, String document, int endOffset) {
		Set<IStrategoTerm> completionContexts;
		SGLR sglr = new SGLR(parseController.getParser().getParser().getTreeBuilder(), parseController.getParser().getParser().getParseTable());
		sglr.setCompletionParse(true, getCompletionOffsetMid());
		int recoverOffset = this.startOffsetCompletionToken - 2;			
		sglrReuser.parsePrefix(sglr, true, true, document, recoverOffset);
		ArrayDeque<Frame> parserConfigDocumentPrefix = sglrReuser.parsePrefix(sglr, false, false, document, getCompletionOffsetMid());
		completionContexts = 
				sglr.findLongestLeftContextReductions(
					parserConfigDocumentPrefix,
					getCompletionOffsetMid(),
					endOffset,
					document
		);
		return completionContexts;
	}

	private Set<IStrategoTerm> focusCompletionContexts(Set<IStrategoTerm> completionContexts, 
			ArrayList<NodeMapping<String>> mappings, boolean isRequiredMatch) {
		Set<IStrategoTerm> result = new java.util.HashSet<IStrategoTerm>();
		for (IStrategoTerm subtree : completionContexts) {
			IStrategoTerm focusedContext = focusCompletionContext(subtree, mappings, isRequiredMatch);
			if(focusedContext != null)
				result.add(focusedContext);
		}
		return result;
	}
	
	private IStrategoTerm focusCompletionContext(IStrategoTerm term, 
			ArrayList<NodeMapping<String>> mappings, boolean isRequiredMatch) {
		if(isSortConstructorMatch(term, mappings))
			return term; //largest syntactic construct that matches sort and form
		for (int i = 0; i < term.getSubtermCount(); i++) {
			IStrategoTerm subTerm = term.getSubterm(i);
			int startOffset = getStartOffset(subTerm);
			int endOffset = getEndOffset(subTerm);
			if(startOffset <= getCompletionOffsetMid() && getCompletionOffsetMid() <= endOffset){
				IStrategoTerm childResult = focusCompletionContext(subTerm, mappings, isRequiredMatch);
				if(childResult != null)
					return childResult;
			}
		}
		if(!checkSortCriterion(term, mappings, isRequiredMatch))
			return null;
		if(term.getSubtermCount() == 0)
			return null; //a bit of context is required
		return term;
	}


	private boolean isSortConstructorMatch(IStrategoTerm term,
			ArrayList<NodeMapping<String>> mappings) {
		return InputTermBuilder.isMatchOnConstructorOrSort(mappings, term);
	}

	private boolean checkSortCriterion(IStrategoTerm trm, ArrayList<NodeMapping<String>> mappings, boolean isRequiredMatch){
		return !isRequiredMatch || isSortConstructorMatch(trm, mappings);
	}
	
	private Set<IStrategoTerm> constructInputTerms(Set<IStrategoTerm> completionContexts) {
		Set<IStrategoTerm> inputTerms = new java.util.HashSet<IStrategoTerm>();
		for (IStrategoTerm completionContext : completionContexts) {
			IStrategoTerm completionAST = constructASTForContext(completionContext);
			try {
				observer.getLock().lock();
				completionAST = analyzeAST(completionAST);
				IStrategoTerm inputTerm = observer.getInputBuilder().makeInputTermResultingAst(completionAST, completionContext, true);
				if(inputTerm != null){
					//completionContext is not in completionAST
					inputTerms.add(inputTerm);
				}
			} catch (Exception e) {
				String errorMessage = "No semantic proposals available - analysis failed for context completion AST";
				Environment.logException(errorMessage, e);
			} finally {
				observer.getLock().unlock();
			}
		}
		return inputTerms;
	}
	
	private IStrategoTerm constructASTForContext(IStrategoTerm completionContext) {
		IStrategoTerm newCompletionAST = ((StrategoTerm)this.currentAST).clone(false);
		String sort = ImploderAttachment.getSort(completionContext);
		IStrategoTerm nodeContext = findCompletionContext(newCompletionAST, sort);
		if(nodeContext != null){
			if(hasSort(nodeContext, sort)){ 
				newCompletionAST = getRoot(replaceNode(nodeContext, completionContext));
			}
			else {
				assert nodeContext.isList();
				IStrategoTerm insertedCompletionContext = insertCompletionContext(nodeContext, completionContext);
				newCompletionAST = getRoot(replaceNode(nodeContext, insertedCompletionContext));			
			}			
		}
		else{
			final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
			IStrategoTerm[] terms = new IStrategoTerm[2];
			terms[0] = newCompletionAST;
			terms[1] = factory.makeAppl(COMPLETION_UNKNOWN, completionContext);
			newCompletionAST = factory.makeTuple(terms);
		}
		return newCompletionAST;		
	}

	private IStrategoTerm findCompletionContext(IStrategoTerm trm, String sort) {
		if(containsCompletionOffset(trm)){
			for (int i = 0; i < trm.getSubtermCount(); i++) {
				IStrategoTerm subterm = trm.getSubterm(i);
				if(containsCompletionOffset(subterm)){
					IStrategoTerm result = findCompletionContext(subterm, sort);
					if(result != null){
						return result; //term that covers the cursor offset and has the right sort
					}
					if(trm.isList()){
						IStrategoTerm altResult = findAlternateReplaceTerm(subterm, sort); 
						if(altResult != null){
							return altResult; //term nearby cursor offset with the right sort, subterm local (cursor offset) scope
						}
					}
				} 
			}
			if (hasSort(trm, sort)) {
				return trm;
			}
			if(trm.isList()){
				return trm; //do not search outside list (most local scope). Instead insert term or no context term.
			}
		}
		return null;
	}

	private IStrategoTerm findAlternateReplaceTerm(IStrategoTerm trm, String sort) {
		for (int i = trm.getSubtermCount() - 1; i >= 0 ; i--) {
			IStrategoTerm subterm = trm.getSubterm(i);
			IStrategoTerm result = findAlternateReplaceTerm(subterm, sort);
			if(result != null){
				return result;
			}
		}
		if (hasSort(trm, sort)) {
			return trm;
		}
		return null;
	}


	private boolean hasSort(IStrategoTerm trm, String sort) {
		return trm != null && ImploderAttachment.getSort(trm) != null && ImploderAttachment.getSort(trm).equals(sort);
	}
	
	private boolean haveSameSort(IStrategoTerm trm1, IStrategoTerm trm2) {
		if(trm1 == null || ImploderAttachment.getSort(trm1) == null || trm2 == null || ImploderAttachment.getSort(trm2) == null){
			return false;
		}
		return ImploderAttachment.getSort(trm2).equals(ImploderAttachment.getSort(trm1)) ||
		ImploderAttachment.getSort(trm2).equals(ImploderAttachment.getElementSort(trm1));
	}


	private IStrategoTerm insertCompletionContext(IStrategoTerm list, IStrategoTerm completionContext) {
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
		assert list.isList();
		boolean sameSort = 
			haveSameSort(list, completionContext); //TODO: find out which
		IStrategoTerm insertedElem = sameSort ? completionContext : factory.makeAppl(COMPLETION_UNKNOWN, completionContext);
		ArrayList<IStrategoTerm> elems = new ArrayList<IStrategoTerm>();
		boolean isInserted = false;
		for (int i = 0; i < list.getSubtermCount(); i++) {
			int startOffset = this.getStartOffset(list.getSubterm(i));
			if(!isInserted && getCompletionOffsetMid() <= startOffset){
				elems.add(insertedElem);
				isInserted = true;
			}
			elems.add(list.getSubterm(i));
		}
		if(!isInserted)
			elems.add(insertedElem);			
		return factory.makeList(elems);
	}

	private IStrategoTerm analyzeAST(IStrategoTerm ast) throws UndefinedStrategyException, //TODO let user specify analyze function in .esv
			InterpreterErrorExit, InterpreterExit, InterpreterException {
		IStrategoTuple inputTermAnalysis = observer.getInputBuilder().makeInputTerm(ast, false);
		File file = SourceAttachment.getFile(ast);
		IStrategoTerm analysisResult = observer.invoke(observer.getFeedbackFunction(), inputTermAnalysis, file);
		ast = TermReader.termAt(analysisResult, 0);
		setParents(ast);
		return ast;
	}

	private void setParents(IStrategoTerm term) {
		for (int i = 0; i < term.getSubtermCount(); i++) {
	    	if (term.getSubterm(i).getStorageType() == MUTABLE){
	    		ParentAttachment.putParent(term.getSubterm(i), term, null);
			} 
			setParents(term.getSubterm(i));
		}
	}


	private Set<IStrategoTerm> putCompletionNodes(Set<IStrategoTerm> inputTerms, String completionPrefix) {
		Set<IStrategoTerm> results = new java.util.HashSet<IStrategoTerm>();
		for (IStrategoTerm inputTuple : inputTerms) {
			ParentAttachment.putParent(inputTuple, null, null);
			ParentAttachment.putParent(termAt(inputTuple, 0), inputTuple, null);
			results.add(replaceCompletionTokenWithCompletionNode(termAt(inputTuple, 0), completionPrefix));
		}
		return results;
	}

	private IStrategoTerm replaceCompletionTokenWithCompletionNode(IStrategoTerm trm, String completionPrefix) {
		IStrategoString completionTokenTerm = getCompletionPrefixNode(trm);
		if(completionTokenTerm != null){
			IStrategoTerm completionNode = makeCompletionNode(completionTokenTerm, completionPrefix);
			if(completionNode != null){
				completionNode = replaceNode(completionTokenTerm, completionNode);
				return getRoot(completionNode);
			}
		}
		return getRoot(trm);
	}
	
	private IStrategoString getCompletionPrefixNode(IStrategoTerm trm) {
		int startOffsetTrm = getStartOffset(trm);
		if (Tools.isTermString(trm) && startOffsetTrm == this.startOffsetCompletionToken) {
			//String prefix = ((IStrategoString) trm).stringValue();
			//assert prefix.contains(COMPLETION_TOKEN); 
			return ((IStrategoString)trm);
		}
		for (int i = 0; i < trm.getSubtermCount(); i++) {
			IStrategoTerm subterm = trm.getSubterm(i);
			IStrategoString result = getCompletionPrefixNode(subterm);
			if(result != null){
				return result;
			} 
		}
		return null;
	}

	private IStrategoTerm makeCompletionNode(IStrategoString prefixCompletionTerm, String completionPrefix){
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
		String prefix = prefixCompletionTerm.stringValue();
		//assert prefix.contains(COMPLETION_TOKEN); //assert prefix.endsWith(COMPLETION_TOKEN); 
		prefix = completionPrefix;              //prefix.substring(0, prefix.length() - COMPLETION_TOKEN.length());
		IStrategoTerm prefixTerm = factory.makeString(prefix);
//		IStrategoTerm[] oldAnnos = prefixCompletionTerm.getAnnotations().getAllSubterms();
//		for (int i = 0; i < oldAnnos.length; i++) {
//			IStrategoTerm anno = oldAnnos[i];
//			if(TermReader.isTermString(anno) && ((IStrategoString)anno).stringValue().equals(COMPLETION_TOKEN)){
//				oldAnnos[i] = prefixCompletionTerm;
//			}
//		}
//		IStrategoList newAnnos = factory.makeList(oldAnnos, factory.makeList());
//		prefixTerm = factory.annotateTerm(prefixTerm, newAnnos);
//		prefixTerm = factory.annotateTerm(prefixTerm, prefixCompletionTerm.getAnnotations());
		factory.copyAttachments(prefixCompletionTerm, prefixTerm, true);			
		IStrategoTerm completionNode = factory.makeAppl(COMPLETION_CONSTRUCTOR, prefixTerm);
		completionNode = factory.annotateTerm(completionNode, prefixCompletionTerm.getAnnotations());
		factory.copyAttachments(prefixCompletionTerm, completionNode, true);
		ParentAttachment.putParent(prefixTerm, completionNode, null);
		return completionNode;
	}
	
	private Set<IStrategoTerm> constructOutputTerms(IParseController controller, Set<IStrategoTerm> inputTerms) {
		Set<IStrategoTerm> proposalStringLists = new java.util.HashSet<IStrategoTerm>();
		for (IStrategoTerm input : inputTerms) { // try all possible input terms 
			IStrategoTerm result = invokeCompletionFunction(controller, input);
			proposalStringLists.add(result);
		}
		return proposalStringLists;
	}

	private IStrategoTerm invokeCompletionFunction(final IParseController controller, IStrategoTerm inputTuple) {
		if (completionFunction == null) {
			return TermFactory.EMPTY_LIST;
		} else {
			IStrategoTerm result = TermFactory.EMPTY_LIST;
			try {
				observer.getLock().lock();
				File file = getFile(TermReader.termAt(inputTuple, 2));
				if(file == null){
					file = controller.getPath().toFile();
				}
				result = observer.invokeSilent(completionFunction, inputTuple, file);
				if (result == null) {
					observer.reportRewritingFailed();
					result = TermFactory.EMPTY_LIST;
				}
			} catch (Exception e) {
				this.errorMessage = "No semantic proposals available - rule failed.";
				Environment.logException(errorMessage, e);
			} finally {
				observer.getLock().unlock();
			}
			return result;
		}
	}

	private Set<Completion> constructProposals(Set<IStrategoTerm> proposalLists) {
		Set<Completion> proposals = new java.util.HashSet<Completion>();
		for (IStrategoTerm proposalList : proposalLists) {
			Set<Completion> result = toCompletions(proposalList);
			if(result != null){
				proposals.addAll(result);
			}
			else {
				this.errorMessage = "No semantic proposals available - invalid result.";
			}
		}		
		return proposals;
	}
	
	private Set<Completion> toCompletions(IStrategoTerm proposals) {

		if (proposals.getTermType() != LIST)
			return null;

		final ITermFactory factory = Environment.getTermFactory();
		final IStrategoString emptyString = factory.makeString("");
		final Set<Completion> results = new HashSet<Completion>();

		for (IStrategoList cons = (IStrategoList) proposals; !cons.isEmpty(); cons = cons.tail()) {
			IStrategoTerm proposal = cons.head();
			IStrategoList newTextParts;
			IStrategoString description;

			switch (proposal.getTermType()) {
				case STRING:
					newTextParts = factory.makeList(proposal);
					description = emptyString;
					break;

				case LIST:
					newTextParts = (IStrategoList) proposal;
					description = emptyString;
					break;

				case TUPLE:
					if (proposal.getSubtermCount() != 2)
						return null;
					IStrategoTerm newTextTerm = termAt(proposal, 0);
					switch (newTextTerm.getTermType()) {
						case STRING: newTextParts = factory.makeList(newTextTerm); break;
						case LIST:   newTextParts = (IStrategoList) newTextTerm;   break;
						default:     return null;
					}
					description = termAt(proposal, 1);
					break;

				default:
					return null;
			}

			// empty list of new text parts is wrong
			if (newTextParts.isEmpty() || termAt(newTextParts, 0).getTermType() != STRING) {
				return null;
			}

			// description must be a string
			if (description.getTermType() != STRING) {
				return null;
			}

			results.add(Completion.makeSemantic(newTextParts, description.stringValue()));
		}

		return results;
	}

	private Set<Completion> filterProposals(String completionPrefix) {
		Set<Completion> filteredProposals = new java.util.HashSet<Completion>();
		for (Completion proposal : this.proposals) {
			if (proposal.extendsPrefix(completionPrefix)) {
				filteredProposals.add(proposal);
			}
		}
		return filteredProposals;
	}
			
	private static IStrategoTerm replaceNode(final IStrategoTerm oldNode, final IStrategoTerm newNode) {
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());

		class Transformer extends TermTransformer {
			IStrategoTerm replacementNode;

			public Transformer() {
				super(factory, true);
			}

			@Override
			public IStrategoTerm preTransform(IStrategoTerm current) {
				if (current == oldNode) {
					replacementNode = newNode;
					factory.copyAttachments(current, replacementNode, true);
					return replacementNode;
				} else {
					return current;
				}
			}
		}

		Transformer trans = new Transformer();
		trans.transform(getRoot(oldNode));
		return trans.replacementNode;
	}

	private int getEndOffset(IStrategoTerm trm) {
		IStrategoTerm imploderOrigin = ImploderAttachment.getImploderOrigin(trm);
		if(imploderOrigin == null){
			if(trm.getSubtermCount() > 0)
				return getEndOffset(trm.getSubterm(trm.getSubtermCount()-1)); //some robustness against losing origin info
			return -1;
		}
		return ImploderAttachment.getRightToken(imploderOrigin).getEndOffset();
	}

	private int getStartOffset(IStrategoTerm trm) {
		IStrategoTerm imploderOrigin = ImploderAttachment.getImploderOrigin(trm);
		if(imploderOrigin == null){
			if(trm.getSubtermCount() > 0)
				return getStartOffset(trm.getSubterm(0)); //some robustness against losing origin info
			return -1;
		}
		org.spoofax.jsglr.client.imploder.IToken startToken = ImploderAttachment.getLeftToken(imploderOrigin);
		if(startToken.getIndex() == 0 && startToken.getEndOffset() == -1 && startToken.getTokenizer().getTokenCount() > 1){ //nonsense token
			startToken = startToken.getTokenizer().getTokenAt(1);
		}
		return startToken.getStartOffset();
	}	

	private void setCompletionContexts(Set<IStrategoTerm> inputTerms) {
		this.completionContexts.clear();
		for (IStrategoTerm trm : inputTerms) {
			completionContexts.add(termAt(trm, 0));			
		}
	}

	public Set<IStrategoTerm> getCompletionContexts() {
		return completionContexts;
	}

	public boolean hasErrors(){
		return this.errorMessage != null && (this.results == null || this.results.isEmpty());
	}
	
	public String getErrorMessage(){
		return this.errorMessage;
	}
	
	private int getCompletionOffsetMid() {
		return this.startOffsetCompletionToken + COMPLETION_TOKEN.length()-3;
	}


	private boolean containsCompletionOffset(IStrategoTerm trm) {
		int startOffsetTrm = getStartOffset(trm);
		int endOffsetTrm = getEndOffset(trm);
		if (startOffsetTrm <= this.startOffsetCompletionToken && isPartOfListSuffixAt(trm, this.startOffsetCompletionToken))
			return true;
		if (startOffsetTrm >= this.startOffsetCompletionToken && isPartOfListPrefixAt(trm, this.startOffsetCompletionToken))
			return true;
		return startOffsetTrm <= this.startOffsetCompletionToken && this.startOffsetCompletionToken <= endOffsetTrm;
	}
	
	/**
	 * Tests if an end offset is part of a list suffix
	 * (considers the layout following the list also part of the list).
	 */
	protected static boolean isPartOfListSuffixAt(IStrategoTerm node, final int offset) {
		return node.isList() && offset <= Tokenizer.findRightMostLayoutToken(getRightToken(node)).getEndOffset();
	}
	
	/**
	 * Tests if an offset is part of a list prefix
	 * (considers the layout preceding the list also part of the list).
	 */
	protected static boolean isPartOfListPrefixAt(IStrategoTerm node, int offset) {
		return node.isList() && offset >= Tokenizer.findLeftMostLayoutToken(getLeftToken(node)).getStartOffset();
	}
	
	private IStrategoTerm[] extendWithInjections(IStrategoTerm[] semanticNodes) {
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
		Set<String> additionalSorts = new HashSet<String>();
		for (IStrategoTerm semanticNode : semanticNodes) {
			if(TermReader.hasConstructor((IStrategoAppl)semanticNode, "Sort")){
				String sort = TermReader.termContents(semanticNode);
				additionalSorts.addAll(sglrReuser.getInjectionsFor(sort));
				additionalSorts.remove(sort);
			}
		}

		IStrategoTerm[] result = Arrays.copyOf(semanticNodes, additionalSorts.size() + semanticNodes.length);
		int index = semanticNodes.length;
		for (String srt : additionalSorts) {
			result[index] =  factory.makeAppl(
				factory.makeConstructor("Sort", 1), 
				factory.makeList(factory.makeString(srt))
			);
			index++;
		}
		return result;
	}



//
//	public Set<Completion> getSemanticProposalsForSort(IParseController controller, String sort, String programPrefix, String trustedSuffix) {
//		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
//		
//		Set<String> injections = sglrReuser.getInjectionsFor(sort);
//		Iterator<String> itInjections = injections.iterator();
//		
//		IStrategoTerm[] semanticSorts = new IStrategoTerm[injections.size()];
//		for (int i = 0; i < semanticSorts.length; i++) {
//			semanticSorts[i] =  factory.makeAppl(
//				factory.makeConstructor("Sort", 1), 
//				factory.makeList(factory.makeString(itInjections.next()))
//			);
//		}
//
//		//calculate 'empty string' proposals
//		return calculateProposals(controller, programPrefix, "", trustedSuffix, semanticSorts, true);
//	}
}
