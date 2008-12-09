package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicFoldingUpdater extends AbstractService<IFoldingUpdater> implements IFoldingUpdater {

	public DynamicFoldingUpdater() {
		super(IFoldingUpdater.class);
	}

	public void updateFoldingStructure(IParseController parseController,
			ProjectionAnnotationModel annotationModel) {
		
		initialize(parseController.getLanguage());
		
		getWrapped().updateFoldingStructure(parseController, annotationModel);
	}

}
