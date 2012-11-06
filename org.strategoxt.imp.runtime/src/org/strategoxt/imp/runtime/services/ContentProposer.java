package org.strategoxt.imp.runtime.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.ParseTable;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.stratego.StrategoConsole;

/**
 * Content completion.
 *
 * @author Maartje de Jonge
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Tobi Vollebregt
 */
public class ContentProposer implements IContentProposer {

	//calculating and managing completion suggestions
	private final Pattern identifierLexical;
	
	private final ContentProposerSyntactic syntacticProposer;

	private final ContentProposerSemantic semanticProposer;

	private final StrategoObserver observer;
	
	private IParseController controller;


	// stored results for reuse //TODO: best to store the shortest prefix since then only filtering needed?
	private ContentProposal[] results;

	private String lastDocumentPrefix;

	private String lastCompletionPrefix;

	private String lastDocumentSuffix;


	// protects results, lastDocument, and lastSelection
	private Object lock = new Object();

	//queue to offer completion results 
	private final BlockingQueue<ICompletionProposal[]> resultsQueue = new ArrayBlockingQueue<ICompletionProposal[]>(1);

	protected void onProposalApplied() {
		observer.setRushNextUpdate(true);
		getParser(controller).getErrorHandler().setRushNextUpdate(true);
		getParser(controller).scheduleParserUpdate(0, false);
	}

	// ensures only one completion job runs at a time
	private ISchedulingRule serializeJobs = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule arg0) {
			return arg0 == serializeJobs; 
		}
		
		public boolean contains(ISchedulingRule arg0) {
			return arg0 == serializeJobs;
		}
	};

	protected Pattern getCompletionLexical() {
		return identifierLexical;
	}

	public ContentProposer(SGLRParseController parseController, StrategoObserver observer, String completionFunction, 
			IStrategoTerm[] semanticNodes, Pattern identifierLexical, Set<Completion> templates) {
		this.observer = observer;
		this.identifierLexical = identifierLexical;
		
		//share the same reuser for performance
		ParseTable pt = parseController != null? parseController.getParser().getParseTable() : null;
		ParseConfigReuser sglrReuser = new ParseConfigReuser(pt);		
		this.syntacticProposer = new ContentProposerSyntactic(templates, sglrReuser);
		this.semanticProposer = new ContentProposerSemantic(observer, completionFunction, semanticNodes, sglrReuser); //TODO semantic nodes
	}

	public ICompletionProposal[] getContentProposals(final IParseController controller, final int offset, final ITextViewer viewer) {
		this.controller = controller;
		final String document = viewer.getDocument().get();
		final Point selectedRange = viewer.getSelectedRange();
		final Position selection = new Position(selectedRange.x, selectedRange.y);
		final int cursorOffset = selection.getOffset() + selection.getLength();
		final String completionPrefix = readPrefix(cursorOffset, document);
		final String documentPrefix = document.substring(0, cursorOffset - completionPrefix.length());
		final String documentSuffix = document.substring(cursorOffset);	
		
		assert (documentPrefix + completionPrefix + documentSuffix).equals(document);

		synchronized(lock) {
			// Any past result waiting?
			if (results != null) {
				// Return that result when still valid.
				if (
					documentPrefix.equals(lastDocumentPrefix) && 
					documentSuffix.equals(lastDocumentSuffix)
				) {
					if(results == null || results.length == 0){
						if(syntacticProposer.hasErrors())
							return createErrorProposal(syntacticProposer.getErrorMessage(), selection);
						if(semanticProposer.hasErrors())
							return createErrorProposal(semanticProposer.getErrorMessage(), selection);
					}			
					if(completionPrefix.equals(lastCompletionPrefix)){
						return results;
					}
					if(completionPrefix.startsWith(lastCompletionPrefix)){
						this.lastCompletionPrefix = completionPrefix;
						this.filterResultsOnPrefix(completionPrefix, selection, viewer);
						return results;
					}
				}				
				results = null;
			}

			// No => kick off a new calculation.
			lastDocumentPrefix = documentPrefix;
			lastCompletionPrefix = completionPrefix;
			lastDocumentSuffix = documentSuffix;
		}
		
		if (!identifierLexical.matcher(ContentProposerSemantic.COMPLETION_TOKEN).matches()) {
			return createErrorProposal(
				"No proposals available - completion lexical must allow the string: '" + ContentProposerSemantic.COMPLETION_TOKEN + "'", selection);
		}


		Job job = new Job("Computing content proposals") {
			private boolean shouldBeCancelled(IProgressMonitor monitor) {
				// Job should be cancelled when lastDocument or lastSelection
				// has been updated by a new call to getContentProposals.
				return monitor.isCanceled()
						|| !documentPrefix.equals(lastDocumentPrefix)
						|| !completionPrefix.equals(lastCompletionPrefix)
						|| !documentSuffix.equals(lastDocumentSuffix);
			}

			@Override
			public IStatus run(IProgressMonitor monitor) {
				// A new request for proposals may have come in before we even started...
				synchronized (lock) {
					if (shouldBeCancelled(monitor))
						return Status.CANCEL_STATUS;
				}
				
				long startTime = System.currentTimeMillis();
				
				// collect syntactic proposals
				Set<Completion> syntacticCompletions = syntacticProposer.getSyntacticCompletions(getParser(controller), documentPrefix, completionPrefix,  documentSuffix);
				
				System.out.println("syntactic: " + (System.currentTimeMillis() - startTime));
				startTime = System.currentTimeMillis();
				
				// collect semantic proposals (REMARK: most efficient to do this here, after syntactic proposals because of SGLR stack reuse)
				Set<Completion> semanticCompletions = semanticProposer.getSemanticCompletions(controller, documentPrefix, completionPrefix, documentSuffix);

				System.out.println("semantic: " + (System.currentTimeMillis() - startTime));
				startTime = System.currentTimeMillis();

				// merge syntactic and semantic completions, create content proposals
				Set<Completion> allCompletions = new HashSet<Completion>();
				allCompletions.addAll(semanticCompletions);
				allCompletions.addAll(syntacticCompletions);
				ContentProposal[] tmpResults = toCompletionProposals(allCompletions, completionPrefix, selection, viewer);
				
				printCompletionTip(controller);
				
				// Offer results to getContentProposals, it might still be waiting.
				// If the offer fails, don't re-trigger completion when we got no results.
				if (resultsQueue.offer(tmpResults) || tmpResults == null || tmpResults.length == 0) {
					results = tmpResults;
					return Status.OK_STATUS;
				}

				// Re-trigger completion.
				// Next invocation of getContentProposals will return the results.
				UIJob job = new UIJob("Re-triggering content assist") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						// Don't re-trigger if document/selection changed
						// between scheduling and execution of this UIJob.
						synchronized(lock) {
							if (shouldBeCancelled(monitor)
									|| viewer.getDocument() == null
									|| !viewer.getDocument().get().equals(document)
									|| !viewer.getSelectedRange().equals(selectedRange)) {
								return Status.CANCEL_STATUS;
							}
						}
						((ITextOperationTarget) viewer).doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.setPriority(Job.INTERACTIVE);
				job.schedule();

				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.setRule(serializeJobs);
		resultsQueue.clear();
		job.schedule();

		try {
			// Wait up to 500 ms, maybe the job finishes quickly.
			// This greatly reduces "flickering" of the completion popup
			// when it is open while typing text or moving the cursor.
			ICompletionProposal[] tmpResults = resultsQueue.poll(500, TimeUnit.MILLISECONDS);

			// Block the queue by putting a dummy element in it.
			// This will signal the job to re-trigger content assist.
			if (!resultsQueue.offer(new ICompletionProposal[0])) {
				// If offer fails then the job finished between the poll and the offer.
				tmpResults = resultsQueue.poll();
			}

			// If tmpResults == null, then popup goes away,
			// will pop up again when the job finishes.

			// Returning a "Be patient" ErrorProposal here doesn't work:
			// the completion re-trigger is ignored if the popup is open...
			if(tmpResults == null || tmpResults.length == 0){
				if(syntacticProposer.hasErrors())
					return createErrorProposal(syntacticProposer.getErrorMessage(), selection);
				if(semanticProposer.hasErrors())
					return createErrorProposal(semanticProposer.getErrorMessage(), selection);
			}			

			return tmpResults;
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	public ContentProposal[] getTemplateProposalsForSort(String wantedSort, ITextViewer viewer) {
		Set<Completion> templatesForSort = syntacticProposer.getTemplateProposalsForSort(getParser(controller), wantedSort);
		final Set<ContentProposal> results = new HashSet<ContentProposal>();
		for (Completion proposal : templatesForSort) {
			results.add(new ContentProposal(this, proposal, viewer));
		}
		return toSortedArray(results);
	}

	private ContentProposal[] toCompletionProposals(Set<Completion> completions, String completionPrefix, Position OffsetPosition, ITextViewer viewer){
		Set<ContentProposal> completionProposals = new HashSet<ContentProposal>();
		for (Completion proposal : completions) {
			completionProposals.add(new ContentProposal(this, proposal, completionPrefix, OffsetPosition, viewer));
		} 
		return toSortedArray(completionProposals); 					
	}

	private static ContentProposal[] toSortedArray(Set<ContentProposal> results) {
		ContentProposal[] resultArray = results.toArray(new ContentProposal[results.size()]);
		Arrays.sort(resultArray, new Comparator<ICompletionProposal>() {
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
			}
		});
		return resultArray;
	}

	private ICompletionProposal[] createErrorProposal(String error, Position selection) {
		return new ICompletionProposal[] { new ErrorProposal(error, selection.getOffset()) };
	}
	
	/**
	 * Read the identifier at the offset location, using
	 * the identifier lexical regular expression.
	 */
	public String readPrefix(int offset, String document) {
		int prefixStart = offset;
		int lastGoodPrefixStart = offset;
		while (--prefixStart >= 0) {
			String prefix = document.substring(prefixStart, offset);
			if (identifierLexical.matcher(prefix).matches()) {
				lastGoodPrefixStart = prefixStart;
			} else if (prefix.charAt(0) == '\n') {
				return document.substring(lastGoodPrefixStart, offset);
			}
		}
		return document.substring(lastGoodPrefixStart, offset);
	}
	
	private void filterResultsOnPrefix(String completionPrefix, Position position, ITextViewer viewer) {
		final Set<ContentProposal> filterResults = new HashSet<ContentProposal>();
		for (int i = 0; i < results.length; i++) {
			if (results[i].getCompletion().extendsPrefix(completionPrefix)) {
				filterResults.add(new ContentProposal(this, results[i].getCompletion(), completionPrefix, position, viewer));
			}			
		}
		this.results = toSortedArray(filterResults); 
	}
	
	public static SGLRParseController getParser(IParseController controller) {
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		return (SGLRParseController) controller;
	}
	
	private void printCompletionTip(IParseController controller) {
		if (Environment.getDescriptor(controller.getLanguage()).isDynamicallyLoaded()) {
			String semanticContexts = "\nSemantic completion nodes: " + semanticProposer.getCompletionContexts();
			String syntacticConstructs = "\nAccepted syntactic constructs: " + syntacticProposer.getAcceptedSorts();
			//String rejectedSyntacticConstructs = "Rejected syntactic constructs: " + syntacticProposer.getRejectedSorts();
			String missingSortWarning = "\n\nWarning missing sorts: " + syntacticProposer.missingSortTemplates();
			try {
				StrategoConsole.getOutputWriter().write(semanticContexts);
				StrategoConsole.getOutputWriter().write(syntacticConstructs);
				//StrategoConsole.getOutputWriter().write(rejectedSyntacticConstructs);
				StrategoConsole.getOutputWriter().write(missingSortWarning);	
			} catch (IOException e) {
				Environment.logWarning("Could not write to console", e);
			}
		}
	}
}
