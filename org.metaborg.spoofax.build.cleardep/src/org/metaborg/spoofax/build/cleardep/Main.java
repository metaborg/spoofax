package org.metaborg.spoofax.build.cleardep;


import java.io.IOException;
import java.util.HashMap;

import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildContext;
import org.sugarj.common.path.AbsolutePath;

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
	
	public static void main(String[] args) throws IOException {
		BuildContext context = new BuildContext();
		Properties props = makeProperties("TempalteLang");
		Clean clean = new Clean(context, props);
		
		if (args.length > 0 && "clean".equals(args[0]))
			clean.require(null, new AbsolutePath("./build.dep"), new SimpleMode());
	}

}
