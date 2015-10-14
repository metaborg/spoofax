package org.strategoxt.imp.metatooling.stratego;

import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.Term;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.RegisteringStrategy;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;
import org.strategoxt.lang.StrategyCollector;
import org.strategoxt.lang.compat.NativeCallHelper;
import org.strategoxt.lang.compat.SSL_EXT_call;
import org.strategoxt.lang.linking.OverridingStrategy;
import org.strategoxt.permissivegrammars.main_make_permissive_0_0;
import org.strategoxt.permissivegrammars.complibrary.lang.StrategoExit;

/**
 * Overrides the xtc-command strategy to use sdf2table from the SDF plugin.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
@OverridingStrategy
public class SDFBundleCommand extends RegisteringStrategy {
	
	protected static final SDFBundleCommand instance = new SDFBundleCommand();

    private static final String NATIVE_PATH = "native/";

    private static final boolean ENABLED = true;

    private Strategy proceed;

    private final String[] windowsEnvironment = createWindowsEnvironment();

    private String binaryPath;

    private String binaryExtension;

    private boolean initialized;

    @Override
    public void registerImplementators(StrategyCollector collector) {
    	System.out.println("Register SDFBundleCommand");
		collector.registerStrategyImplementator("xtc_command_1_0", instance);
	}
	
    @Override
	public void bindExecutors(StrategyCollector collector) {
    	System.out.println("Bind SDFBundleCommand");
    	proceed = collector.getStrategyExecutor("xtc_command_1_0", this);
	}

    
    public void init() throws IOException {
        if(initialized)
            return;
        System.out.println("Initialze SDFBundleCommand");
        binaryPath = getBinaryPath();
        binaryExtension = getBinaryExtension();

        if(isLinuxOS() || isMacOS()) {
            EditorIOAgent agent = new EditorIOAgent();
            boolean success = makeExecutable(agent, "sdf2table") && makeExecutable(agent, "implodePT");
            if(!success)
                Environment.logException("chmod of native tool bundle executables failed:\n" + agent.getLog());
        }
        Debug.log("Initialized the native tool bundle in " + getBinaryPath());
        initialized = true;
    }

    public String getBinaryPath() throws IOException, UnsupportedOperationException {
        if(System.getenv("SPOOFAX_NATIVE_PATH") != null)
            return System.getenv("SPOOFAX_NATIVE_PATH");

        String subdir;
        if(isLinuxOS()) {
            subdir = "linux";
        } else if(isWindowsOS()) {
            subdir = "cygwin";
        } else if(isMacOS()) {
            subdir = "macosx";
        } else {
            throw new UnsupportedOperationException("Platform is not supported"); // TODO: print platform
        }

        File result = null;
        final Bundle nativeBundle = Platform.getBundle("org.strategoxt.imp.nativebundle");
		if (nativeBundle != null) {
        	final IPath path = new Path(NATIVE_PATH + subdir);
        	final URL url = FileLocator.find(nativeBundle, path, null);
        	result = new File(FileLocator.toFileURL(url).getPath());
		} else {
			// Fallback in the case that OSGI is not initialized
			// Query classpath
			String classpath = System.getProperty("java.class.path");
			java.util.List<String> classpathEntries = new java.util.ArrayList<>(Arrays.asList(classpath
							.split(File.pathSeparator)));
			for (String entry : classpathEntries) {
				if (entry.contains("org.strategoxt.imp.nativebundle")) {
					result = new File(entry + "/" + NATIVE_PATH + subdir);
					break;
				}
			}
			if (result == null) {
				throw new IOException("Unable to find nativebundle");
			}
		}

        if(!result.exists())
            throw new FileNotFoundException(result.getAbsolutePath());
        return result.getAbsolutePath() + File.separator;
    }

    public String getBinaryExtension() {
        return isWindowsOS() ? ".exe" : "";
    }

    public static SDFBundleCommand getInstance() {
        return (SDFBundleCommand) instance;
    }

    @Override 
    public IStrategoTerm invoke(Context context, IStrategoTerm args, org.strategoxt.lang.Strategy commandStrategy) {
    	System.out.println("Invoke SDFBundleCommand");
        try {
            init();
        } catch(IOException e) {
            Environment.logException(
                "Could not determine the binary path for the native tool bundle (" + System.getProperty("os.name")
                    + "/" + System.getProperty("os.arch") + ")", e);
            return proceed.invoke(context, args, commandStrategy);
        } catch(RuntimeException e) {
            Environment.logException("Failed to initialize the native tool bundle (" + System.getProperty("os.name")
                + "/" + System.getProperty("os.arch") + ")", e);
            return proceed.invoke(context, args, commandStrategy);
        }

        IStrategoTerm commandTerm = commandStrategy.invoke(context, args);
        System.out.println("Command Term: " + commandTerm);
        if(!ENABLED || commandTerm.getTermType() != STRING)
            return proceed.invoke(context, args, commandStrategy);

        // TODO: Hack: make-permissive is called through this too (because it cannot be compiled by the separate compiler due to missing sources)
        
        String command = ((IStrategoString) commandTerm).stringValue();
        
        if (command.equals("make-permissive")) {
        	return  invokeMakePermissive(context, args);
        }
        
        
        if(!new File(binaryPath + command + binaryExtension).exists()) {
        	System.out.println("File does not exist");
            if(command.equals("sdf2table") || command.equals("implodePT")) {
                throw new StrategoException("Could not find the native tool bundle command " + command + " in "
                    + binaryPath + command + binaryExtension);
            }
            return proceed.invoke(context, args, commandStrategy);
        }
        
        System.out.println("Args: " + args);

        if(args.getTermType() != LIST)
            return null;

        if(isLinuxOS() || isMacOS()) {
            if(!makeExecutable(context.getIOAgent(), command)) {
                EditorIOAgent io = (EditorIOAgent) context.getIOAgent();
                Environment.logException("chmod of " + binaryPath + command + binaryExtension + " failed, log:\n"
                    + io.getLog());
                return proceed.invoke(context, args, commandStrategy); // (already logged)
            }
        }
        
        System.out.println("INVOKE");
        boolean success = invoke(context, command, ((IStrategoList) args).getAllSubterms());
        System.out.println("Scuess : " + success );
        return success ? args : null;
    }

    private IStrategoTerm invokeMakePermissive(Context context, IStrategoTerm args) {
    	// See: SSL_EXT_java_call; rather similar but cannot be used here because of special classes for make-permissive
    	System.out.println("Invoke make permissive with " + args);
    	String oldWorkingDir = context.getIOAgent().getWorkingDir();
    	org.strategoxt.permissivegrammars.complibrary.lang.Context makePermissiveContext =
    			new org.strategoxt.permissivegrammars.complibrary.lang.Context(context.getFactory(), context.getIOAgent());
    	org.strategoxt.permissivegrammars.Main.init(makePermissiveContext);
    	// Need to prepend program name into the list
    	args = context.getFactory().makeListCons(context.getFactory().makeString("make-permissive"), (IStrategoList) args);
    	try {
    		IStrategoTerm result = main_make_permissive_0_0.instance.invoke(makePermissiveContext, args);
   	    	return result;
    	} catch (StrategoExit e) {
    		return e.getValue() == StrategoExit.SUCCESS ? args : null;
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException (e);
    	} finally {
    		try {
    		context.getIOAgent().setWorkingDir(oldWorkingDir);
    		} catch (Exception e) {}
    	}
    	
    }
    
    public boolean invoke(Context context, String command, IStrategoTerm[] argList) {
        String[] commandArgs = SSL_EXT_call.toCommandArgs(binaryPath + command, argList);
        // Disabled this check since Windows x64 might identify differently?
        // String[] environment = isWindowsOS()
        // ? createWindowsEnvironment()
        // : null;
        System.out.println("Command args: " + Arrays.toString(commandArgs));
        String[] environment = windowsEnvironment;
        IOAgent io = context.getIOAgent();

        try {
            if(commandArgs == null)
                return false;
            Writer out = new OutputStreamWriter(System.out);//io.getWriter(IOAgent.CONST_STDOUT);
            Writer err = new OutputStreamWriter(System.err);//io.getWriter(IOAgent.CONST_STDERR);
            System.out.println("Invoke Native tool");
            err.write("Invoking native tool " + binaryPath + command + binaryExtension + " " + Arrays.toString(argList)
                + "\n");
            int result = new NativeCallHelper().call(commandArgs, environment, new File(io.getWorkingDir()), out, err);
            System.out.println("Result: " + result);
            if(result != 0) {
                Environment.logException("Native tool " + command + " exited with error code " + result
                    + "\nCommand:\n  " + Arrays.toString(commandArgs) + "\nEnvironment:\n "
                    + Arrays.toString(environment) + "\nWorking dir:\n  " + io.getWorkingDir());
            }
            return result == 0;
        } catch(IOException e) {
            throw new StrategoException("Could not call native tool " + command + "\nCommand:\n  "
                + Arrays.toString(commandArgs) + "\nEnvironment:\n " + Arrays.toString(environment)
                + "\nWorking dir:\n  " + io.getWorkingDir(), e);
        } catch(InterruptedException e) {
            throw new StrategoException("Could not call " + command, e);
        } catch(RuntimeException e) {
            throw new StrategoException("Could not call native tool " + command + "\nCommand:\n  "
                + Arrays.toString(commandArgs) + "\nEnvironment:\n " + Arrays.toString(environment)
                + "\nWorking dir:\n  " + io.getWorkingDir(), e);
        }
    }

    private static String[] createWindowsEnvironment() {
        Map<String, String> envp = new HashMap<String, String>(System.getenv());
        envp.put("CYGWIN", "nodosfilewarning");
        String[] result = new String[envp.size()];
        int i = 0;
        for(Entry<String, String> entry : envp.entrySet()) {
            result[i++] = entry.getKey() + "=" + entry.getValue();
        }
        return result;
    }

    private boolean makeExecutable(IOAgent io, String command) {
        try {
            Writer out = io.getWriter(IOAgent.CONST_STDOUT);
            Writer err = io.getWriter(IOAgent.CONST_STDERR);
            command = binaryPath + command + binaryExtension;
            // /bin/sh should exist even on NixOS
            String[] commandArgs = { "/bin/sh", "-c", "chmod +x \"" + command + "\"" };
            int result = new NativeCallHelper().call(commandArgs, new File(binaryPath), out, err);
            return result == 0;
        } catch(InterruptedException e) {
            Environment.logException("chmod failed: /bin/sh -c \"chmod +x " + command + "\"", e);
            return false;
        } catch(IOException e) {
            Environment.logException("chmod failed: /bin/sh -c \"chmod +x " + command + "\"", e);
            return false;
        }
    }

    private boolean isLinuxOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux");
    }

    private boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}
