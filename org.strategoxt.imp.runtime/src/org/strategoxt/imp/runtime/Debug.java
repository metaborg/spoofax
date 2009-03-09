package org.strategoxt.imp.runtime;

import java.util.Stack;

/**
 * General debugging functionality for IMP SGLR plugin.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * */
public class Debug {	
	public static final boolean ENABLED = Debug.class.desiredAssertionStatus();
	
	private static ThreadLocal<Stack<Long>> timers = new ThreadLocal<Stack<Long>>();

	public static Stack<Long> getTimers() {
		Stack<Long> result = timers.get();
		if (result == null) {
			result = new Stack<Long>();
			timers.set(result);
		}
		
		return result;
	}

	public static void log(Object... messageParts) {
		if (ENABLED) {
			StringBuilder message = new StringBuilder("[org.strategoxt.imp] ");
			
			for (Object s : messageParts) {
				message.append(s);
			}
			
			System.out.println(message);
		}
	}
	
	public static void startTimer(Object... messageParts) {
		if (ENABLED) {
			log(messageParts);
			getTimers().push(System.currentTimeMillis());
		}
	}
	
	public static void startTimer() {
		if (ENABLED) {
			getTimers().push(System.currentTimeMillis());
		}
	}
	
	public static void stopTimer(String message) {
		if (ENABLED) {
			long start = getTimers().pop();
			
			log(message, "-", System.currentTimeMillis() - start, " ms");
		}		
	}
}
