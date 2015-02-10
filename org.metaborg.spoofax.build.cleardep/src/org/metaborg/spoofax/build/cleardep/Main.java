package org.metaborg.spoofax.build.cleardep;


import java.io.IOException;
import java.util.HashMap;

import org.metaborg.spoofax.build.cleardep.builders.All;
import org.metaborg.spoofax.build.cleardep.builders.Clean;
import org.metaborg.spoofax.build.cleardep.builders.ForceOnSave;
import org.metaborg.spoofax.build.cleardep.builders.PPPack;
import org.metaborg.spoofax.build.cleardep.builders.SpoofaxDefaultCtree;
import org.strategoxt.HybridInterpreter;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Main {

	private static Properties makeProperties(String lang) {
		Properties props = new Properties(new HashMap<String, String>());
		
		props.put("sdfmodule", lang);
		props.put("metasdfmodule", "Stratego-" + lang);
		props.put("esvmodule", lang);
	    props.put("strmodule", lang.substring(0, 1).toLowerCase() + lang.substring(1));
	    props.put("ppmodule", lang + "-pp");
	    props.put("sigmodule", lang + "-sig"); 
	    
	    props.put("trans", "trans");
	    props.put("trans.rel", "trans");
	    props.put("src-gen", "editor/java");
	    props.put("syntax", "src-gen/syntax");
	    props.put("syntax.rel", props.get("syntax"));
	    props.put("include", "include");
	    props.put("include.rel", props.get("include"));
	    props.put("lib", "lib");
	    props.put("build", "target/classes");
	    props.put("dist", "bin/dist");
	    props.put("pp", "src-gen/pp");
	    props.put("signatures", "src-gen/signatures");
	    props.put("sdf-src-gen", "src-gen");
	    props.put("lib-gen", "include");
	    props.put("lib-gen.rel", props.get("lib-gen"));
		
		return props;
	}
	
	public static Clean clean;
	public static All all;
	public static PPPack ppPack;
	public static SpoofaxDefaultCtree spoofaxDefaultCtree;
	public static ForceOnSave forceOnSave;
	
	private static void initBuilders(SpoofaxBuildContext context) {
		clean = new Clean(context);
		all = new All(context);
		ppPack = new PPPack(context);
		spoofaxDefaultCtree = new SpoofaxDefaultCtree(context);
		forceOnSave = new ForceOnSave(context);
	}
	
	public static void main(String[] args) throws IOException {
		Log.log.setLoggingLevel(Log.ALWAYS);
		
		if (args.length <= 0)
			throw new IllegalArgumentException("Require base-dir path as first argument.");
		Path baseDir = new AbsolutePath(args[0]);
		Path binDir = new RelativePath(baseDir, "include");
		
		Properties props = makeProperties("TempalteLang");
		SpoofaxBuildContext context = new SpoofaxBuildContext(baseDir, binDir, props, new HybridInterpreter());
		initBuilders(context);
		
		if (args.length > 1 && "clean".equals(args[1]))
			clean.require(null, context.basePath("${include}/build.clean.dep"), new SimpleMode());
		else
			all.require(null, context.basePath("${include}/build.all.dep"), new SimpleMode());
	}

}
