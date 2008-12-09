package org.strategoxt.imp.metatooling.building;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicDescriptorBuilder {
	private Interpreter builder;
	
	private Map<String, Set<String>> mainEditorFiles = new HashMap<String, Set<String>>();
	
	public DynamicDescriptorBuilder() throws InterpreterException, IOException {
		builder = Environment.createInterpreter();
		builder.setIOAgent(new TrackingIOAgent());
		builder.load(DynamicDescriptorBuilder.class.getResourceAsStream("/include/sdf2imp.ctree"));
	}
	
	public void updateFile(String filename) throws InterpreterException {
		Set<String> mainFiles = mainEditorFiles.get(filename);
		// TODO
		
		builder.invoke("dr-scope-all-start");
		builder.setCurrent(builder.getFactory().makeString(filename));
		boolean success = builder.invoke("sdf2imp-for-file");
		builder.invoke("dr-scope-all-end");
		
		Set<String> tracked = ((TrackingIOAgent) builder.getIOAgent()).getTracked();
		tracked.clear();
	}
	
	private boolean isMainFile(String file) {
		// TODO: Determine if a file is the main descriptor file by its contents?
		// InputStream stream = builder.getIOAgent().openInputStream(file);
		
		return file.matches("(/[^-\\.]*\\.esv|-Main\\.esv|\\.main\\.esv)$");
	}
}
