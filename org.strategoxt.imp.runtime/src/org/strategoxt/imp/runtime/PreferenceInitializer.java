package org.strategoxt.imp.runtime;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.strategoxt.imp.runtime.statistics.EditStreakRecorder;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = RuntimeActivator.getInstance().getPreferenceStore();
		store.setDefault(SpoofaxPreferencePage.COLLECT_EDIT_SCENARIOS, false);
		store.setDefault(EditStreakRecorder.PREF_ENABLE_STREAK, false);
		store.setDefault(EditStreakRecorder.PREF_STREAK_EXPIRY, EditStreakRecorder.DEFAULT_TIMEOUT);
	}
}
