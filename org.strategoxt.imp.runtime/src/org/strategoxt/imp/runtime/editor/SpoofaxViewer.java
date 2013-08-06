package org.strategoxt.imp.runtime.editor;

import org.eclipse.imp.editor.StructuredSourceViewer;
import org.eclipse.imp.editor.UniversalEditor.StructuredSourceViewerConfiguration;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * A viewer is a model-based adapter on a widget. In case of Spoofax, these widgets come in the form of
 * editor services (e.g. hover help, reference resolution).
 * 
 * We subclass {@link ProjectionViewer} rather than {@link SourceViewer} because we want to have support
 * for code folding.
 * 
 * Note: Javadoc of {@link ProjectionViewer} states "do not subclass". This may be an error in the Javadoc.
 * Subclassing makes since we want to reuse its functionality, which is not provided by any other Eclipse
 * class.
 * 
 * @author Oskar van Rest
 */
public class SpoofaxViewer extends ProjectionViewer {

	StructuredSourceViewer structuredSourceViewer;
	
	/**
	 * Text operation code for requesting the outline for the current input.
	 */
	public static final int SHOW_OUTLINE = 51;

	private IInformationPresenter spoofaxOutlinePresenter;

	public SpoofaxViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		structuredSourceViewer = new StructuredSourceViewer(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
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
        super.configure(configuration);
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
