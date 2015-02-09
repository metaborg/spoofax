package org.metaborg.spoofax.build.cleardep;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
