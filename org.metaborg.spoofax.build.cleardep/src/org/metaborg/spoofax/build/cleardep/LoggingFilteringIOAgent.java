package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.spoofax.interpreter.library.IOAgent;
import org.sugarj.common.Log;
import org.sugarj.common.util.PrintStreamWriter;

public class LoggingFilteringIOAgent extends IOAgent {

	/**
	 * Regex exclusion patterns. If any of the patterns matches a given logging
	 * message completely, this message is ignored by this IOAgent.
	 */
	private final List<Pattern> excludePatterns;

	private final LoggingWriter outWriter;
	private final LoggingWriter errWriter;

	public LoggingFilteringIOAgent(String... regexs) {
		outWriter = new LoggingWriter(new PrintStreamWriter(Log.out));
		errWriter = new LoggingWriter(new PrintStreamWriter(Log.err));
		excludePatterns = new LinkedList<Pattern>();
		for (String regex : regexs)
			excludePatterns.add(Pattern.compile(regex, Pattern.DOTALL));
	}

	@Override
	public Writer getWriter(int fd) {
		switch (fd) {
		case IOAgent.CONST_STDOUT:
			return outWriter;
		case IOAgent.CONST_STDERR:
			return errWriter;
		default:
			return super.getWriter(fd);
		}
	}

	public String getOutLog() {
		return outWriter.getLog();
	}

	public String getErrLog() {
		return errWriter.getLog();
	}

	private class LoggingWriter extends Writer {

		private final Writer writer;
		private StringBuilder log = new StringBuilder();
		private boolean skip;
		private StringBuilder msg;

		public LoggingWriter(Writer writer) {
			this.writer = writer;
			this.skip = false;
			this.msg = new StringBuilder();
		}

		public String getLog() {
			return log.toString();
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			String s = new String(cbuf, off, len);

			if (skip) {
				skip = !s.endsWith("\n");
				return;
			}

			msg.append(s);
			for (Pattern pat : excludePatterns)
				if (pat.matcher(s).matches()) {
					skip = !s.endsWith("\n");
					log.append(msg);
					msg = new StringBuilder();
					return;
				}

			writer.write(cbuf, off, len);
			log.append(cbuf, off, len);
		}

		@Override
		public void flush() throws IOException {
			writer.flush();
		}

		@Override
		public void close() throws IOException {
			writer.close();
		}

	}

}