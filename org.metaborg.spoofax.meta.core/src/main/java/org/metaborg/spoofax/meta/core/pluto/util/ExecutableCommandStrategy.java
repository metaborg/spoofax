package org.metaborg.spoofax.meta.core.pluto.util;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.Strings;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;
import org.strategoxt.lang.compat.SSL_EXT_call;
import org.strategoxt.stratego_xtc.xtc_command_1_0;
import org.sugarj.common.Exec;

public class ExecutableCommandStrategy extends xtc_command_1_0 {
    private static final ILogger log = LoggerUtils.logger("Build log");

    private final String command;
    private final File executable;
    private transient final xtc_command_1_0 proceed;


    public ExecutableCommandStrategy(String command, File executable) {
        this.command = command;
        this.executable = executable;
        synchronized(xtc_command_1_0.class) {
            this.proceed = xtc_command_1_0.instance;
            xtc_command_1_0.instance = this;
        }
    }


    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy commandStrat) {
        final IStrategoTerm commandTerm = commandStrat.invoke(context, current);
        if(TermUtils.isString(commandTerm) && TermUtils.isList(current)) {
            final String command = ((IStrategoString) commandTerm).stringValue();
            if(this.command.equals(command)) {
                final ExecutionResult result = run(current.getAllSubterms());
                return result.success ? current : null;
            }
        }

        return proceed.invoke(context, current, commandStrat);
    }


    public ExecutionResult run(IStrategoTerm... args) {
        final String[] commandArgs = SSL_EXT_call.toCommandArgs(executable.getAbsolutePath(), args);
        return runInternal(commandArgs);
    }

    public ExecutionResult run(String... args) {
        return runInternal(ArrayUtils.add(args, 0, executable.getAbsolutePath()));
    }

    private ExecutionResult runInternal(String[] commandAndArgs) {
        final String[] args = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);
        log.info("Execute {} {}", command, Strings.join(Arrays.asList(args), " "));
        final Exec.ExecutionResult result = (new Exec(false)).runWithPrefix(commandAndArgs[0], (File)null, commandAndArgs);
        return new ExecutionResult(true, StringUtils.join(result.outMsgs, '\n'),
            StringUtils.join(result.errMsgs, '\n'));
    }
}
