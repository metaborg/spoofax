package org.strategoxt.imp.runtime;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.strategoxt.imp.runtime.RuntimeActivator;

/**
 * 
 */
public class SpoofaxPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public static final String COLLECT_EDIT_SCENARIOS = "collectEditScenariosPreference";

	public SpoofaxPreferencePage() {
		super(GRID);
		setPreferenceStore(RuntimeActivator.getInstance().getPreferenceStore());
		setDescription("Preferences for language development with Spoofax");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(
			new BooleanFieldEditor(
				COLLECT_EDIT_SCENARIOS,
				"&Collect Edit Scenarios",
				getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		//
	}
	
}