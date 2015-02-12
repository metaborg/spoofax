package org.metaborg.spoofax.build.cleardep;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.strategoxt.imp.metatooling.JarsAntPropertyProvider;
import org.sugarj.common.path.Path;

public class Properties {
	private Map<String, String> props;
	
	public Properties(Map<String, String> props) {
		this.props = props;
	}
	
	public String substitute(String in) {
		String s = in;
		int dollar = -1;
		while ((dollar = s.indexOf("$", dollar)) >= 0 && s.length() > dollar+1 && s.charAt(dollar+1) == '{') {
			int begin = dollar + 2;
			int end = s.indexOf('}', begin);
			String key = s.substring(begin, end);
			String val = props.get(key);
			if (val != null)
				s = s.replaceAll(Pattern.quote("${" + key + "}"), Matcher.quoteReplacement(val));
			else
				dollar = end;
		}
		return s;
	}
	
	public void put(String key, String val) {
		props.put(key, val);
	}
	
	public String get(String key) {
		return props.get(key);
	}

	public Object getOrFail(String key) {
		String val = get(key);
		if (val == null)
			throw new IllegalArgumentException("Undefined property " + key);
		return val;
	}
	
	public String getOrElse(String key, String defaultVal) {
		String val = get(key);
		if (val == null)
			return defaultVal;
		return val;
	}
	
	public boolean isDefined(String key) {
		return props.containsKey(key);
	}
	
	public static Properties makeSpoofaxProperties(String lang, Path[] sdfImports) {
		Properties props = new Properties(new HashMap<String, String>());

		props.put("sdfmodule", lang);
		props.put("metasdfmodule", "Stratego-" + lang);
		props.put("esvmodule", lang);
		props.put("strmodule", lang.substring(0, 1).toLowerCase() + lang.substring(1));
		props.put("ppmodule", lang + "-pp");
		props.put("sigmodule", lang + "-sig");

		props.put("trans", "trans");
		props.put("src-gen", "editor/java");
		props.put("syntax", "src-gen/syntax");
		props.put("include", "include");
		props.put("lib", "lib");
		props.put("build", "target/classes");
		props.put("dist", "bin/dist");
		props.put("pp", "src-gen/pp");
		props.put("signatures", "src-gen/signatures");
		props.put("sdf-src-gen", "src-gen");
		props.put("lib-gen", "include");
		
		if (sdfImports != null) {
			StringBuilder importString = new StringBuilder();
			for (Path imp : sdfImports)
				importString.append("-Idef " + props.substitute(imp.getAbsolutePath()));
			props.put("build.sdf.imports", importString.toString());
		}
		
		props.put("eclipse.spoofaximp.jars", new JarsAntPropertyProvider().getAntPropertyValue("eclipse.spoofaximp.jars"));

		return props;
	}
}
