package org.metaborg.spoofax.meta.core.pluto.util;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.lang.compat.SSL_EXT_call;
import org.strategoxt.stratego_xtc.xtc_command_1_0;
import org.sugarj.common.Exec;
import org.sugarj.common.Log;

import com.google.common.base.Objects;

public class ExecutableCommandStrategy extends xtc_command_1_0 implements Externalizable {
	private static final long serialVersionUID = -8725205705069536170L;

	private final static Map<String, ExecutableCommandStrategy> instances = new HashMap<>();

	public static ExecutableCommandStrategy getInstance(String command) {
		return instances.get(command);
	}

	public static ExecutableCommandStrategy getInstance(String command, File exe) {
		ExecutableCommandStrategy strategy = instances.get(command);
		if (strategy == null) {
			strategy = new ExecutableCommandStrategy(command, exe);
			instances.put(command, strategy);
		}
		if (!strategy.exe.equals(exe))
			throw new IllegalArgumentException("Existing strategy for command " + command + " has different executable. Existing: " + strategy.exe
					+ ". This request: " + exe);
		return strategy;
	}

	private xtc_command_1_0 proceed;

	private String command;
	private File exe;

	/**
	 * @deprecated for deserialization only
	 */
	@Deprecated
	public ExecutableCommandStrategy() {
	}

	private ExecutableCommandStrategy(String command, File exe) {
		init(command, exe);
	}

	private void init(String command, File exe) {
		synchronized (xtc_command_1_0.class) {
			this.proceed = xtc_command_1_0.instance;
			xtc_command_1_0.instance = this;
		}
		this.command = command;
		this.exe = exe;
	}

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy commandStrat) {
		IStrategoTerm commandTerm = commandStrat.invoke(context, current);
		if (commandTerm instanceof IStrategoString && current instanceof IStrategoList) {
			String command = ((IStrategoString) commandTerm).stringValue();
			if (this.command.equals(command)) {
				ExecutionResult result = run(current.getAllSubterms());
				return result.success ? current : null;
			}
		}

		return proceed.invoke(context, current, commandStrat);
	}

	public ExecutionResult run(IStrategoTerm... args) {
		String[] commandArgs = SSL_EXT_call.toCommandArgs(exe.getAbsolutePath(), args);
		return runInternal(commandArgs);
	}

	public ExecutionResult run(String... args) {
		return runInternal(ArrayUtils.add(args, 0, exe.getAbsolutePath()));
	}

	private ExecutionResult runInternal(String[] commandAndArgs) {
		boolean success = false;
		try {
			Log.log.beginTask("Execute sdf2table", Log.DETAIL);
			Exec.ExecutionResult result = Exec.run(commandAndArgs);
			success = true;
			return new ExecutionResult(true, StringUtils.join(result.outMsgs, '\n'), StringUtils.join(result.errMsgs, '\n'));
		} finally {
			Log.log.endTask(success);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(command);
		out.writeObject(exe);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		String command = (String) in.readObject();
		File exe = (File) in.readObject();
		ExecutableCommandStrategy other = getInstance(command);
		if (other == null) {
			init(command, exe);
			instances.put(command, this);
		} else {
			this.command = command;
			this.exe = exe;
			this.proceed = other.proceed;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExecutableCommandStrategy))
			return false;

		ExecutableCommandStrategy other = (ExecutableCommandStrategy) obj;
		return Objects.equal(command, other.command) && Objects.equal(exe, other.exe);
	}
}