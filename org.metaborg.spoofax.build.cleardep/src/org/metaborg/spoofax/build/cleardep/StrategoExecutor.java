package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.library.IOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.sugarj.common.Log;
import org.sugarj.common.util.PrintStreamWriter;

public class StrategoExecutor {

	public static class ExecutionResult {
		public final boolean success;
		public final String outLog;
		public final String errLog;

		public ExecutionResult(boolean success, String outLog, String errLog) {
			this.success = success;
			this.outLog = outLog;
			this.errLog = errLog;
		}
	}

	/**
	 * 
	 * @return true if run successfully, false if the strategy failed
	 */
	public static <S extends Strategy> ExecutionResult runStrategoCLI(Context strategoContext, S strat, String desc, Object... args) {
		List<String> sargs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			String str = args[i].toString();
			for (String s : str.split("[ \t\r\n]"))
				if (!s.isEmpty())
					sargs.add(s);
		}

		LoggingIOAgent agent = new LoggingIOAgent();
		try {
			strategoContext.setIOAgent(agent);
			strategoContext.invokeStrategyCLI(strat, desc, sargs.toArray(new String[sargs.size()]));
			return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
		} catch (StrategoExit e) {
			if (e.getValue() == 0)
				return new ExecutionResult(true, agent.getOutLog(), agent.getErrLog());
			return new ExecutionResult(false, agent.getOutLog(), agent.getErrLog());
		}
	}

	
	public static class LoggingIOAgent extends IOAgent {

		private final LoggingWriter outWriter = new LoggingWriter(new PrintStreamWriter(Log.out));
		private final LoggingWriter errWriter = new LoggingWriter(new PrintStreamWriter(Log.err));

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

			public LoggingWriter(Writer writer) {
				this.writer = writer;
			}
			
			public String getLog() {
				return log.toString();
			}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				log.append(cbuf, off, len);
				writer.write(cbuf, off, len);
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
}
