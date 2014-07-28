package org.strategoxt.imp.runtime.services.views.outline;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class SpoofaxOutlinePopupFactory {

	public PopupDialog create(Shell parent, IParseController parseController, Composite editorComposite) {
		int shellStyle = SpoofaxOutlinePopup.INFOPOPUP_SHELLSTYLE;
    	int treeStyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
    	return new SpoofaxOutlinePopup(parent, shellStyle, treeStyle, parseController, editorComposite);
	}
}
