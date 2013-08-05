package org.strategoxt.imp.runtime.editor;

import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

public class SpoofaxSourceViewer extends SourceViewer {

	public SpoofaxSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
	}

}
