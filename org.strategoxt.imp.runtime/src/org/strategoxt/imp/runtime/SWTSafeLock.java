/**
 * 
 */
package org.strategoxt.imp.runtime;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.widgets.Display;

/**
 * A Reentrant lock implementation that ensures the SWT event loop runs
 * while acquiring a lock from the main thread, thus avoiding a certain
 * class of dead locks.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SWTSafeLock extends ReentrantLock {

	private static final long serialVersionUID = 1448450448343689240L;
	
	private static final long INITIAL_TIMEOUT = 30000;

	private static final long EVENT_RATE = 50;
	
	public SWTSafeLock(boolean fair) {
		super(fair);
	}
	
	public SWTSafeLock() {
		// Default constructor (unfair)
	}

	@Override
	public void lock() {
		if (Environment.isMainThread()) {
			try {
				// TODO: Could the SWTSafeLock cause trouble, e.g. by launching multiple content proposers?
				//       Let's use an INITIAL_TIMEOUT to be safe
				if (!tryLock(INITIAL_TIMEOUT, TimeUnit.MILLISECONDS)) {
					do {
						while (Display.getCurrent().readAndDispatch());
					} while (!tryLock(EVENT_RATE, TimeUnit.MILLISECONDS));
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			super.lock();
		}
	}
}
