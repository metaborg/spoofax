package org.strategoxt.imp.runtime.editor;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IEditorAssociationOverride;

/**
 * @author Oskar van Rest
 * 
 * This editor association override makes sure that when a file is opened with IMPs editor, it is opened with Spoofax' editor instead.
 * This feature was created to support legacy Eclipse workspaces that were created before SpoofaxEditor.java came to live.
 */
public class IMPEditorAssociationOverride implements IEditorAssociationOverride {

	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,	IEditorDescriptor[] editorDescriptors) {
		return editorDescriptors;
	}

	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		return editorDescriptors;
	}

	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType, IEditorDescriptor editorDescriptor) {
		return overrideDefaultEditor(editorInput.getName(), contentType, editorDescriptor);
	}

	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType, IEditorDescriptor editorDescriptor) {
		if (editorDescriptor != null && editorDescriptor.getId().equals("org.eclipse.imp.runtime.impEditor")) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			return registry.findEditor(SpoofaxEditor.EDITOR_ID);
		}

		return editorDescriptor;
	}
}
