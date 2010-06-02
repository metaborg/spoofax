package org.strategoxt.imp.runtime.services;


public class StrategoAnalysisQueueFactory {

	private static final StrategoAnalysisQueue INSTANCE = new StrategoAnalysisQueue();
	
	public static StrategoAnalysisQueue getInstance() {
		return INSTANCE; 
	}
	
}
