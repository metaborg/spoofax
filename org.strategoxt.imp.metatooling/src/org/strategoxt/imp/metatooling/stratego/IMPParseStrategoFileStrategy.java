package org.strategoxt.imp.metatooling.stratego;

import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.SGLRException;
import org.strategoxt.imp.editors.stratego.StrategoSugarParseController;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.services.MetaFile;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.strc.parse_stratego_file_0_0;

/**
 * Parse Stratego files with unmanaged parse table support for .meta files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStrategoFileStrategy extends parse_stratego_file_0_0 {
	
	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
		if (current.getTermType() != STRING)
			return null;
		
		String file = ((IStrategoString) current).stringValue();
		try {
			JSGLRI parser = processMetaFile(file, getStrategoParser());
			InputStream stream = null;
			try {
				stream = context.getIOAgent().openInputStream(file);
				IStrategoTerm ast = parser.parse(stream, file);
				SourceAttachment.putSource(ast, EditorIOAgent.getResource(new File(file)), null);
				return ast;
			} finally {
				if (stream != null) stream.close();
			}
		} catch (SGLRException e) {
			context.getIOAgent().printError("parse-stratego-file (" + file + "): " + e.getMessage());
			Environment.logException("Parsing of " + file + " failed", e);
			return null;
		} catch (InvalidParseTableException e) {
			context.getIOAgent().printError("parse-stratego-file (" + file + "): " + e.getMessage());
			Environment.logException("Parsing of " + file + " failed", e);
			return null;
		} catch (IOException e) {
			return null;
		} catch (RuntimeException e) {
			context.getIOAgent().printError("parse-stratego-file (" + file + "): " + e.getMessage());
			Environment.logException("Parsing of " + file + " failed", e);
			return null;
		}
	}

	private JSGLRI getStrategoParser() {
		try {
			Language strategoSugar = LanguageRegistry.findLanguage(StrategoSugarParseController.LANGUAGE);
			JSGLRI parser = new JSGLRI(Environment.getParseTableProvider(strategoSugar), "Module");
			if (parser.getParseTable().hasRecovers()) parser.setUseRecovery(true);
			return parser;
		} catch (BadDescriptorException e) {
			throw new StrategoException("Could not load stratego parse table", e);
		} catch (InvalidParseTableException e) {
			throw new StrategoException("Could not load stratego parse table", e);
		} catch (IOException e) {
			throw new StrategoException("Could not load stratego parse table", e);
		} catch (CoreException e) {
			throw new StrategoException("Could not load stratego parse table", e);
		} catch (RuntimeException e) {
			throw new StrategoException("Could not load stratego parse table", e);
		}
	}

	private JSGLRI processMetaFile(String file, JSGLRI parser) throws InvalidParseTableException {
		String metaFileName = file.substring(0, file.length() - 4) + ".meta";
		MetaFile metaFile = MetaFile.read(metaFileName);
		ParseTable table = null;
		if (metaFile != null) {
			table = Environment.getUnmanagedParseTable(metaFile.getLanguage() + "-Permissive");
			if (table == null) table = Environment.getUnmanagedParseTable(metaFile.getLanguage());
			if (table == null) throw new InvalidParseTableException("No parse table for language " + metaFile.getLanguage());
			parser.setParseTable(table);
			parser.setStartSymbol(null);
			parser.getDisambiguator().setHeuristicFilters(metaFile.isHeuristicFiltersEnabled());
		}
		return parser;
	}
}
