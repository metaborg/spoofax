package org.strategoxt.imp.runtime;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySource;

public class SpoofaxEditor extends UniversalEditor {

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			super.getAdapter(adapter); // TODO
		}

		if (adapter == IPropertySource.class) {
			super.getAdapter(adapter); // TODO
		}

		return super.getAdapter(adapter);
	}
}
