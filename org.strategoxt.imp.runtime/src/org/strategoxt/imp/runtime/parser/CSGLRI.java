package org.strategoxt.imp.runtime.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;
import org.strategoxt.imp.runtime.stratego.NativeCallHelper;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;

/**
 * IMP IParser implementation using the native C version of SGLR, imploding
 * parse trees to AST nodes and tokens.
 * 
 * @note This class currently neither portable nor very efficient, and simply
 *       uses a native call to the SGLR executable.
 * 
 * @see JSGLRI The pure Java version of this class.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class CSGLRI extends AbstractSGLRI {
	
	private final NativeCallHelper caller = new NativeCallHelper();
	
	private final byte[] buffer = new byte[4096];
	
	private final File parseTable;
	
	public CSGLRI(InputStream parseTable, String startSymbol, SGLRParseController controller, TokenKindManager tokenManager) throws IOException {
		super(controller, tokenManager, startSymbol);
		
		// Write this parse table (which may come from a JAR) to disk
		this.parseTable = streamToTempFile(parseTable);
	}

	public CSGLRI(InputStream parseTable, String startSymbol) throws IOException {
		this(parseTable, startSymbol, null, new TokenKindManager());
	}
	
	private File streamToTempFile(InputStream input) throws IOException {
		OutputStream output = null;
		
		try {
			File result = File.createTempFile("parsetable", null);
			output = new FileOutputStream(result);
			
			for (int read = 0; read != -1; read = input.read(buffer)) {
				output.write(buffer, 0, read);
			}
			
			return result;			
		} finally {
			input.close();
			if (output != null) output.close();
		}
	}

	@Override
	public ATerm parseNoImplode(char[] inputChars, String filename) throws SGLRException, IOException {
		ATermFactory factory = Environment.getWrappedATermFactory().getFactory();
		File outputFile = File.createTempFile("parserOutput", null);
		File inputFile = filename == null
				? streamToTempFile(toByteStream(inputChars))
				: new File(filename);
		
		try {
			String[] commandArgs = {
					"sglr", "-p", parseTable.getAbsolutePath(),
					"-i", inputFile.getAbsolutePath(),
					"-o", outputFile.getAbsolutePath(),
					"-s", getStartSymbol(),
					"-2"
			};
			caller.call(commandArgs, null, System.out, System.err);
			
			ATermAppl result = (ATermAppl) factory.readFromFile(outputFile.getAbsolutePath());
			
			if ("error".equals(result.getName()))
				throw new SGLRException("CSGLR Parse error"); // (actual error isn't extracted atm)
			
			return result;
		} catch (InterruptedException e) {
			throw new RuntimeException("CSGLRI parser interrupted", e);
		} finally {
			outputFile.delete();
			inputFile.delete();
		}
	}

}
