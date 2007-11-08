package org.strategoxt.imp.runtime;

import java.util.Stack;

/**
 * General debugging functionality for IMP SGLR plugin.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * */
public class Debug {	
	public static final boolean ENABLED = Debug.class.desiredAssertionStatus();
	
	public static Stack<Long> timers;
	
	public static void log(Object... messageParts) {
		if (ENABLED) {
			StringBuilder message = new StringBuilder("[org.strategoxt.imp]");
			
			for (Object s : messageParts) {
				message.append(' ');
				message.append(s);
			}
			
			System.out.println(message);
		}
	}
	
	public static void startTimer(Object... messageParts) {
		if (ENABLED) {
			log(messageParts);
			timers.push(System.currentTimeMillis());
		}
	}
	
	public static void startTimer() {
		if (ENABLED) {
			timers.push(System.currentTimeMillis());
		}
	}
	
	public static void stopTimer(String message) {
		if (ENABLED) {
			long start = timers.pop();
			
			log(message, ": ", System.currentTimeMillis() - start, "ms");
		}		
	}
}
