package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.imp.services.base.DefaultAutoIndentStrategy;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.strategoxt.imp.runtime.services.views.outline.SpoofaxOutlinePopupFactory;

/**
 * Design decisions:
 * 
 * The Javadoc of {@link ProjectionViewer} states not to subclass it. This may be an error in the Javadoc.
 * Subclassing makes sense because we need support for code folding. Similar functionality is not provided
 * by any other class.
 * 
 * IMP code: methods doToggleComment, calculateLeadingSpace, linesHaveCommentPrefix, doCorrectIndentation
 * and lookingAtLineEnd have been copied from IMP's StructuredSourceViewer (latest version on GitHub).
 * Optimally, we would properly delegate instead. There are three reasons why we choose not to:
 * 1. Instantiating a StructuredSourceViewer is not possible without providing a Composite parent, but we
 *    don't have such a parent (we don't want to give it our parent). We could have extended the IMP patch
 *    to make the required methods public and static, such that we don't need to instantiate a
 *    StructuredSourceViewer. However, this is not possible because one of the methods makes a call to a
 *    protected method of StructuredSourceViewer's superclass. Also, the resulting code wouldn't make much
 *    sense.
 * 2. The copied code is actually not part of IMP's revision that Spoofax is based on. Instead, it was 
 *    backported from a later revision and copied into org.eclipse.imp.runtime.patch. Moving the code from
 *    org.eclipse.imp.runtime.patch to SpoofaxViewer did not result in more code than there already was.
 * 3. It is only temporarily. As Spoofax is in the process of decoupling itself from IMP, the copied code
 *    will sooner or later be replaced.
 */
/**
 * A spoofax viewer is a {@link ProjectionViewer} that in addition provides support for the following editor
 * services:
 * <ul>
 * <li>Quick Outline</li>
 * <li>Toggle Comment</li>
 * <li>Indent Selection</li>
 * </ul>
 * 
 * This class currently shows much similarity to IMP's StructuredSourceViewer (parts have been copied from it).
 * 
 * @author Oskar van Rest
 */
public class SpoofaxViewer extends ProjectionViewer {
	
	/**
	 * Text operation code for requesting the outline for the current input.
	 */
	public static final int SHOW_OUTLINE = 51;

	/**
	 * Text operation code for toggling the commenting of a selected range of text, or the current line.
	 */
	public static final int TOGGLE_COMMENT = 54;
	
    /**
     * Text operation code for indenting the currently selected text.
     */
    public static final int INDENT_SELECTION = 60;
    
    private PopupDialog spoofaxOutlinePopup;
    private IAutoEditStrategy fAutoEditStrategy;
    
	private IParseController parseController;
	private Composite parent;
	
	public SpoofaxViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles, IParseController parseController) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		this.parseController = parseController;
		this.parent = parent;
    }
	
	/*
	 * @see ITextOperationTarget#canDoOperation(int)
	 */
	@Override
	public boolean canDoOperation(int operation) {
		switch (operation) {
	        case SHOW_OUTLINE:
	            return spoofaxOutlinePopup != null;
	        case TOGGLE_COMMENT:
	            return true;
	        case INDENT_SELECTION:
	        	return fAutoEditStrategy != null;
	        }
		
		return super.canDoOperation(operation);
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		switch (operation) {
	        case SHOW_OUTLINE:
				spoofaxOutlinePopup.open();
	            return;
	        case TOGGLE_COMMENT:
	        	doToggleComment(); // "delegate"
	            return;
	        case INDENT_SELECTION:
	        	doCorrectIndentation();// "delegate"
	            return;
		}
		
		super.doOperation(operation);
	}
	
	/*
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	@Override
	public void configure(SourceViewerConfiguration configuration) {
		if (configuration instanceof SpoofaxSourceViewerConfiguration) {
			
        	spoofaxOutlinePopup = new SpoofaxOutlinePopupFactory().create(getControl().getShell(), parseController, parent);
			
			/**
			 * Copied from IMP's StructuredSourceViewer
			 */
            if (fAutoIndentStrategies != null) {
                List<org.eclipse.jface.text.IAutoEditStrategy> strategies= (List<org.eclipse.jface.text.IAutoEditStrategy>) fAutoIndentStrategies.get(IDocument.DEFAULT_CONTENT_TYPE);
                // TODO If there are multiple IAudoEditStrategy's, we may pick up one that doesn't do indent. How to identify the right one?
                // SMS 5 Aug 2008:  There's another problem here, in that the available strategy here
                // may not be of type IAutoEditStrategy.  See bug #243212.  To provide at least a
                // short term fix, I'm going to substitute an appropriate value when that turns out 
                // to be the case.  This may be revised if we decide to somehow avoid the possibility
                // that a strategy of an inappropriate type might appear here.
                if (strategies != null && strategies.size() > 0) {
//                    fAutoEditStrategy= (IAutoEditStrategy) strategies.get(0);
                	if (strategies.get(0) instanceof IAutoEditStrategy)
                		fAutoEditStrategy= (IAutoEditStrategy) strategies.get(0);
                	else
                		fAutoEditStrategy = new DefaultAutoIndentStrategy();
                }
            }
		}
		super.configure(configuration);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
	 */
	@Override
	public void unconfigure() {
		super.unconfigure();
	}
	
	/**
	 * Copied from IMP's StructuredSourceViewer. See design decision on top of document.
	 */
    private void doToggleComment() {
    	ILanguageSyntaxProperties syntaxProps = parseController.getSyntaxProperties();
    	
        if (syntaxProps == null)
            return;

        IDocument doc= this.getDocument();
        DocumentRewriteSession rewriteSession= null;
        Point p= this.getSelectedRange();
        final String lineCommentPrefix= syntaxProps.getSingleLineCommentPrefix();

    	if (doc instanceof IDocumentExtension4) {
    	    IDocumentExtension4 extension= (IDocumentExtension4) doc;
    	    rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
    	}

    	try {
            final int selStart= p.x;
            final int selLen= p.y;
            final int selEnd= selStart + selLen;
            final int startLine= doc.getLineOfOffset(selStart);
            int endLine= doc.getLineOfOffset(selEnd);

            if (selLen > 0 && lookingAtLineEnd(doc, selEnd))
                endLine--;

            boolean linesAllHaveCommentPrefix= linesHaveCommentPrefix(doc, lineCommentPrefix, startLine, endLine);
        	boolean useCommonLeadingSpace= true; // take from a preference?
			int leadingSpaceToUse= useCommonLeadingSpace ? calculateLeadingSpace(doc, startLine, endLine) : 0;

            for(int line= startLine; line <= endLine; line++) {
                int lineStart= doc.getLineOffset(line);
                int lineEnd= lineStart + doc.getLineLength(line) - 1;

                if (linesAllHaveCommentPrefix) {
                	// remove the comment prefix from each line, wherever it occurs in the line
                	int startOffset = lineStart;
                    while (Character.isWhitespace(doc.getChar(startOffset)) && startOffset < lineEnd) {
                        startOffset++;
                    }
                    int endOffset = startOffset + lineCommentPrefix.length();
                    if (Character.isWhitespace(doc.getChar(endOffset)) && endOffset < lineEnd) {
                    	endOffset++;
                    }
                    // The first non-whitespace characters *must* be the single-line comment prefix
                    doc.replace(startOffset, endOffset-startOffset, "");
                } else {
                	// add the comment prefix to each line, after however many spaces leadingSpaceToAdd indicates
                	int offset= lineStart + leadingSpaceToUse;
                	doc.replace(offset, 0, lineCommentPrefix + " ");
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            if (doc instanceof IDocumentExtension4) {
                IDocumentExtension4 extension= (IDocumentExtension4) doc;
                extension.stopRewriteSession(rewriteSession);
            }
            restoreSelection();
        }
    }
	
	/**
	 * Copied from IMP's StructuredSourceViewer. See design decision on top of document.
	 */
    private int calculateLeadingSpace(IDocument doc, int startLine, int endLine) {
    	try {
        	int result= Integer.MAX_VALUE;
        	for(int line= startLine; line <= endLine; line++) {
        		int lineStart= doc.getLineOffset(line);
        		int lineEnd= lineStart + doc.getLineLength(line) - 1;
        		int offset= lineStart;
        		while (Character.isWhitespace(doc.getChar(offset)) && offset < lineEnd) {
        			offset++;
        		}
        		int leadingSpaces= offset - lineStart;
				result= Math.min(result, leadingSpaces);
        	}
    		return result;
    	} catch (BadLocationException e) {
    		return 0;
    	}
	}

	
	/**
	 * Copied from IMP's StructuredSourceViewer. See design decision on top of document.
	 */
    private boolean linesHaveCommentPrefix(IDocument doc, String lineCommentPrefix, int startLine, int endLine) {
    	try {
    		int docLen= doc.getLength();

    		for(int line= startLine; line <= endLine; line++) {
                int lineStart= doc.getLineOffset(line);
                int lineEnd= lineStart + doc.getLineLength(line) - 1;
                int offset= lineStart;

                while (Character.isWhitespace(doc.getChar(offset)) && offset < lineEnd) {
                    offset++;
                }
                if (docLen - offset > lineCommentPrefix.length() && doc.get(offset, lineCommentPrefix.length()).equals(lineCommentPrefix)) {
                	// this line starts with the single-line comment prefix
                } else {
                	return false;
                }
            }
    	} catch (BadLocationException e) {
    		return false;
    	}
		return true;
	}
	
	/**
	 * Copied from IMP's StructuredSourceViewer. See design decision on top of document.
	 */
	private void doCorrectIndentation() {
        IDocument doc= this.getDocument();
        DocumentRewriteSession rewriteSession= null;
        Point p= this.getSelectedRange();

        if (doc instanceof IDocumentExtension4) {
            IDocumentExtension4 extension= (IDocumentExtension4) doc;
            rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
        }

        try {
            final int selStart= p.x;
            final int selLen= p.y;
            final int selEnd= selStart + selLen;
            final int startLine= doc.getLineOfOffset(selStart);
            int endLine= doc.getLineOfOffset(selEnd);

        	// If the selection extends just to the beginning of the next line, don't indent that one too
            if (selLen > 0 && lookingAtLineEnd(doc, selEnd)) {
                endLine--;
            }

            // Indent each line using the AutoEditStrategy
            for(int line= startLine; line <= endLine; line++) {
                int lineStartOffset= doc.getLineOffset(line);

                // Replace the existing indentation with the desired indentation.
                // Use the language-specific AutoEditStrategy, which requires a DocumentCommand.
                DocumentCommand cmd= new DocumentCommand() { };
                cmd.offset= lineStartOffset;
                cmd.length= 0;
                cmd.text= Character.toString('\t');
                cmd.doit= true;
                cmd.shiftsCaret= false;
//              boolean saveMode= fAutoEditStrategy.setFixMode(true);
                fAutoEditStrategy.customizeDocumentCommand(doc, cmd);
//              fAutoEditStrategy.setFixMode(saveMode);
                doc.replace(cmd.offset, cmd.length, cmd.text);
            }
        } catch (BadLocationException e) {
            RuntimePlugin.getInstance().logException("Correct Indentation command failed", e);
        } finally {
            if (doc instanceof IDocumentExtension4) {
                IDocumentExtension4 extension= (IDocumentExtension4) doc;
                extension.stopRewriteSession(rewriteSession);
            }
            restoreSelection();
        }
    }
	
	/**
	 * Copied from IMP's StructuredSourceViewer. See design decision on top of document.
	 */
    private boolean lookingAtLineEnd(IDocument doc, int pos) {
        String[] legalLineTerms= doc.getLegalLineDelimiters();
        try {
            for(String lineTerm: legalLineTerms) {
                int len= lineTerm.length();
                if (pos > len && doc.get(pos - len, len).equals(lineTerm)) {
                    return true;
                }
            }
        } catch (BadLocationException e) {
            RuntimePlugin.getInstance().logException("Error examining document for line termination", e);
        }
        return false;
    }

	// BEGIN: SUPPORT FOR PROPERTIES VIEW

	@SuppressWarnings("rawtypes")
	private List fPostSelectionChangedListeners;
	
	@Override
	protected void handleDispose() {
		super.handleDispose();
		if (fPostSelectionChangedListeners != null)  {
			fPostSelectionChangedListeners.clear();
			fPostSelectionChangedListeners= null;
		}
	}

	/**
	 * Copy of TextViewer#addPostSelectionChangedListener(ISelectionChangedListener)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addPostSelectionChangedListener(ISelectionChangedListener listener)  {

		Assert.isNotNull(listener);

		if (fPostSelectionChangedListeners == null)
			fPostSelectionChangedListeners= new ArrayList();

		if (!fPostSelectionChangedListeners.contains(listener))
			fPostSelectionChangedListeners.add(listener);
	}

	/**
	 * Copy of TextViewer#removePostSelectionChangedListener(ISelectionChangedListener)
	 */
	@Override
	public void removePostSelectionChangedListener(ISelectionChangedListener listener)  {

		Assert.isNotNull(listener);

		if (fPostSelectionChangedListeners != null)  {
			fPostSelectionChangedListeners.remove(listener);
			if (fPostSelectionChangedListeners.size() == 0)
				fPostSelectionChangedListeners= null;
		}
	}
	
	public void firePostSelectionChanged(StrategoTermSelection selection) {
		SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
		firePostSelectionChanged(event);
	}
	
	/**
	 * Copy of TextViewer#firePostSelectionChanged(int, int)
	 */
	@Override
	protected void firePostSelectionChanged(int offset, int length) {
		if (redraws()) {
			IRegion r= widgetRange2ModelRange(new Region(offset, length));
			ISelection selection= r != null ? new TextSelection(getDocument(), r.getOffset(), r.getLength()) : TextSelection.emptySelection();
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
			firePostSelectionChanged(event);
		}
	}
	
	/**
	 * Copy of TextViewer#firePostSelectionChanged(SelectionChangedEvent)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void firePostSelectionChanged(SelectionChangedEvent event) {
		List listeners= fPostSelectionChangedListeners;
		if (listeners != null) {
			listeners= new ArrayList(listeners);
			for (int i= 0; i < listeners.size(); i++) {
				ISelectionChangedListener l= (ISelectionChangedListener) listeners.get(i);
				l.selectionChanged(event);
			}
		}
	}
	
	// END: SUPPORT FOR PROPERTIES VIEW
}