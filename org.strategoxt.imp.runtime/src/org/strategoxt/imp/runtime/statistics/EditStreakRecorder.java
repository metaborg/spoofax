/**
 * 
 */
package org.strategoxt.imp.runtime.statistics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.services.AutoEditStrategy;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * Records edits made to files. It detects and packs edit streaks into single
 * versions automatically if this option is enabled.
 * 
 * Edits are submitted by {@link AutoEditStrategy} through calls to the
 * {@link #recordEdit(IStrategoTerm)} method. Calls to the
 * {@link #recordEdit(IStrategoTerm)} method are asynchronous, this class
 * manages its own thread and uses it to perform background processing.
 * 
 * 
 * Auto-chunking works like this:
 * 
 * All edits are continuously recorded. If no edits are made for at least
 * {@link #PREF_STREAK_EXPIRY} milliseconds the edits are packed and the diffs
 * are committed.
 * 
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class EditStreakRecorder implements Runnable, IPropertyChangeListener {

	/* preference IDs */
	public static final String PREF_ENABLE_STREAK = "streakPrefRecordEnable";

	public static final String PREF_STREAK_EXPIRY = "streakPrefStreakExpiry";

	public static final String RECORD_LOCATION = "_recordedstreaks";

	public static final int DEFAULT_TIMEOUT = 600;

	private boolean cfgEnabled;

	private int cfgStreakExp;

	private Thread thread;

	private final BlockingQueue<RecordedEdit> queue;

	private EditStreak currentStreak;

	private volatile boolean doStop;

	private EditStreakRecorder() {
		this.queue = new LinkedBlockingQueue<RecordedEdit>();
		final IPreferenceStore store = RuntimeActivator.getInstance().getPreferenceStore();
		this.cfgEnabled = store.getBoolean(PREF_ENABLE_STREAK);
		this.cfgStreakExp = store.getInt(PREF_STREAK_EXPIRY);
		init();
	}

	private static EditStreakRecorder INSTANCE;

	public static EditStreakRecorder INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new EditStreakRecorder();
		}
		return INSTANCE;
	}

	/**
	 * Enqueue recording of the given ast. The edited path is determined from
	 * the {@link SourceAttachment} of the AST.
	 * 
	 * @param ast
	 */
	public void recordEdit(IStrategoTerm ast) {
		assert ast != null;
		if (this.thread == null || !this.thread.isAlive() || doStop) {
			return;
		}
		queue.add(new RecordedEdit(ast, System.currentTimeMillis()));
	}

	public void run() {
		while (!doStop) {
			RecordedEdit edit = null;
			try {
				edit = queue.poll(cfgStreakExp, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// irrelevant
			}
			if (currentStreak == null) {
				currentStreak = new EditStreak();
			}

			final long lastStreakEdit = currentStreak.getLastEdit();
			final long now = System.currentTimeMillis();
			if (lastStreakEdit >= 0 && currentStreak.getLastEdit() + cfgStreakExp < now) {
				currentStreak.pack();
				currentStreak.commit();
				currentStreak = new EditStreak();
			}

			if (edit != null) {
				currentStreak.addEdit(edit);
			}
		}
		if (currentStreak != null) {
			currentStreak.commit();
		}
	}

	private void start() {
		if (this.thread == null || !this.thread.isAlive()) {
			this.doStop = false;
			this.thread = new Thread(this);
			this.thread.setDaemon(true);
			this.thread.setName("EditStreakRecorder");
			this.thread.setPriority(Thread.MIN_PRIORITY);
			this.thread.start();
		}
	}

	private void stop() {
		if (this.thread != null && this.thread.isAlive()) {
			System.out.println("Stopping...");
			this.doStop = true;
			this.thread.interrupt();
		}
	}

	public void init() {
		if (cfgEnabled) {
			if (this.thread == null || !this.thread.isAlive()) {
				start();
			}
		} else {
			if (this.thread != null && this.thread.isAlive()) {
				stop();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty() == PREF_ENABLE_STREAK) {
			this.cfgEnabled = (Boolean) event.getNewValue();
			init();
		} else if (event.getProperty() == PREF_STREAK_EXPIRY) {
			this.cfgStreakExp = (Integer) event.getNewValue();
			init();
		}
	}

}
