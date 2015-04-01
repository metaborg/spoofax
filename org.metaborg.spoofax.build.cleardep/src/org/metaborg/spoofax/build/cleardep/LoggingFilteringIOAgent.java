package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.io.OutputStream;
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
	
	public OutputStream getOutStream() {
		return outWriter.getStream();
	}

	public OutputStream getErrStream() {
		return errWriter.getStream();
	}

	public class LoggingWriter extends Writer {

		private final Writer writer;
		private StringBuilder log = new StringBuilder();
		private StringBuilder msg;

		public LoggingWriter(Writer writer) {
			this.writer = writer;
			this.msg = new StringBuilder();
		}

		public String getLog() {
			try {
				this.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return log.toString();
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			boolean lineEnd = (off + len - 1 < 0) || cbuf[off + len - 1] == '\n';
			
			msg.append(cbuf, off, len);
			if (lineEnd) {
				writeString(msg.toString());
				msg = new StringBuilder();
			}
		}

		private void writeString(String s) throws IOException {
			log.append(s);
			for (Pattern pat : excludePatterns)
				if (pat.matcher(s).matches())
					return;
			writer.append(s);
		}

		@Override
		public void flush() throws IOException {
			writeString(msg.toString());
			msg = new StringBuilder();
			writer.flush();
		}

		@Override
		public void close() throws IOException {
			writer.close();
		}

		public OutputStream getStream() {
			return new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					LoggingWriter.this.write(b);
				}
			};
		}
	}

}