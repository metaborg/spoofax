package org.metaborg.spoofax.meta.core;

public interface IBuildStep {
	/**
	 * Pre-Java compilation
	 */
	void compilePreJava(SpoofaxLanguageSpecBuildInput input);
	
	/**
	 * Post-Java compilation
	 */
	void compilePostJava(SpoofaxLanguageSpecBuildInput input);
}