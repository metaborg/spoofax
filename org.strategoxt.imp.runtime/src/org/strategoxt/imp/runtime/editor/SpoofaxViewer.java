package org.strategoxt.imp.runtime.editor;

import org.eclipse.imp.editor.UniversalEditor.StructuredSourceViewerConfiguration;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Oskar van Rest
 */
public class SpoofaxViewer extends ProjectionViewer {

	/**
	 * Text operation code for requesting the outline for the current input.
	 */
	public static final int SHOW_OUTLINE = 51;

	private IInformationPresenter spoofaxOutlinePresenter;

	public SpoofaxViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler,
			boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
	}

	/*
	 * @see ITextOperationTarget#canDoOperation(int)
	 */
	@Override
	public boolean canDoOperation(int operation) {
		switch (operation) {
			case SHOW_OUTLINE:
				return spoofaxOutlinePresenter != null;
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
				if (spoofaxOutlinePresenter != null)
					spoofaxOutlinePresenter.showInformation();
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

        	StructuredSourceViewerConfiguration sSVConfiguration= ((SpoofaxSourceViewerConfiguration) configuration).getStructuredSourceViewerConfiguration();

        	spoofaxOutlinePresenter= sSVConfiguration.getOutlinePresenter(this); // TODO: replace with SpoofaxOutlinePresenter
            if (spoofaxOutlinePresenter != null)
            	spoofaxOutlinePresenter.install(this);
        }
    }
	
    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     */
    @Override
	public void unconfigure() {
        if (spoofaxOutlinePresenter != null) {
        	spoofaxOutlinePresenter.uninstall();
        	spoofaxOutlinePresenter= null;
        }
        super.unconfigure();
    }
}
