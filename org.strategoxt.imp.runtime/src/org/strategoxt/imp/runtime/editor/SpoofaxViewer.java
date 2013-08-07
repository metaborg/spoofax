package org.strategoxt.imp.runtime.editor;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.strategoxt.imp.runtime.services.outline.SpoofaxOutlinePopup;
import org.strategoxt.imp.runtime.services.outline.SpoofaxOutlinePopupFactory;

/**
 * @author Oskar van Rest
 */
public class SpoofaxViewer extends ProjectionViewer {

	/**
	 * Text operation code for requesting the outline for the current input.
	 */
	public static final int SHOW_OUTLINE = 51;
	private PopupDialog spoofaxOutlinePopup;

	public SpoofaxViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, 	boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
	}

	/*
	 * @see ITextOperationTarget#canDoOperation(int)
	 */
	@Override
	public boolean canDoOperation(int operation) {
		switch (operation) {
			case SHOW_OUTLINE:
				return spoofaxOutlinePopup != null;
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
				if (spoofaxOutlinePopup != null) {
					spoofaxOutlinePopup.create();
					((SpoofaxOutlinePopup) spoofaxOutlinePopup).setSize(400, 322); // could not find a way to set default/minimum size constraints
					spoofaxOutlinePopup.open();
				}
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
        	spoofaxOutlinePopup = new SpoofaxOutlinePopupFactory().create(getControl().getShell());
        }
    }
	
    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     */
    @Override
	public void unconfigure() {
        super.unconfigure();
    }
}
