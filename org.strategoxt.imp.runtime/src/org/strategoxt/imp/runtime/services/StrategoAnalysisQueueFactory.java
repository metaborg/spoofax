package org.strategoxt.imp.runtime.services;


public class StrategoAnalysisQueueFactory {

	private static StrategoAnalysisQueue INSTANCE;
	
	public static StrategoAnalysisQueue getInstance() {
		if (INSTANCE == null) initialize();
		return INSTANCE; 
	}
	
	public static void initialize() {
		if (INSTANCE == null) {
			INSTANCE = new StrategoAnalysisQueue();
		}
	}
	
	public static void initialize(StrategoAnalysisQueue queue) {
		INSTANCE = queue;
	}
	
}
