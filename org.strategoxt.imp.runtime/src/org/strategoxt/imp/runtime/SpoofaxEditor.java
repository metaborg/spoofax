package org.strategoxt.imp.runtime;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.strategoxt.imp.runtime.services.SpoofaxOutlinePage;

public class SpoofaxEditor extends UniversalEditor {

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			return new SpoofaxOutlinePage(EditorState.getEditorFor(getParseController()));
		}

		if (adapter == IPropertySource.class) {
			// TODO
		}

		return super.getAdapter(adapter);
	}
}
