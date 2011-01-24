package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.spoofax.interpreter.terms.IStrategoNamed;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.compat.NativeCallHelper;

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
	
	public CSGLRI(InputStream parseTable, String startSymbol, SGLRParseController controller) throws IOException {
		super(parseTable, startSymbol, controller);
		
		// Write this parse table (which may come from a JAR) to disk
		this.parseTable = streamToTempFile(parseTable);
	}

	public CSGLRI(InputStream parseTable, String startSymbol) throws IOException {
		this(parseTable, startSymbol, null);
	}
	
	private File streamToTempFile(InputStream input) throws IOException {
		OutputStream output = null;
		
		try {
			File result = File.createTempFile("temp", null);
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
	protected IStrategoTerm doParse(String inputChars, String filename) throws SGLRException, IOException {
		ITermFactory factory = Environment.getTermFactory();
		File outputFile = File.createTempFile("parserOutput", null);
		File inputFile = filename == null || !new File(filename).exists()
				? streamToTempFile(toByteStream(inputChars))
				: new File(filename);
		String startSymbol = getStartSymbol();
				
		try {
			File tempFile = File.createTempFile("csglri", null);
			String[] parseCommand = {
					"sglr", "-p", parseTable.getAbsolutePath(),
					"-i", inputFile.getAbsolutePath(),
					"-o", isImplodeEnabled() ? tempFile.getAbsolutePath() : outputFile.getAbsolutePath(),
					(startSymbol == null ? "" : "-s"),
					(startSymbol == null ? "" : startSymbol),
					"-2"
			};
			caller.call(parseCommand, null, System.out, System.err);
			if (isImplodeEnabled()) {
				String[] implodeCommand = {
						"implode-asfix",
						"-i", tempFile.getAbsolutePath(),
						"-o", outputFile.getAbsolutePath()
				};
				caller.call(implodeCommand, null, System.out, System.err);
			}
			
			TermReader reader = new TermReader(factory);
			IStrategoNamed result = (IStrategoNamed) reader.parseFromFile(outputFile.getAbsolutePath());
			
			if ("error".equals(result.getName()))
				throw new SGLRException(null, "CSGLR Parse error: " + result); // (actual error isn't extracted atm)
			
			return result;
		} catch (InterruptedException e) {
			throw new RuntimeException("CSGLRI parser interrupted", e);
		} finally {
			outputFile.delete();
			inputFile.delete();
		}
	}
	
	private static ByteArrayInputStream toByteStream(String text) {
		// FIXME: CSGLRI.toByteStream() breaks extended ASCII support
		byte[] bytes = new byte[text.length()];
		
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) text.charAt(i);
		
		return new ByteArrayInputStream(bytes);
	}

}
